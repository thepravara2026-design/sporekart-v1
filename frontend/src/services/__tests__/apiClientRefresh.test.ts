import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from '../apiClient';

const makeAxiosError = (status: number, config: unknown): AxiosError => {
  const error = new AxiosError('Request failed', 'ERR_BAD_REQUEST', config as never);
  error.response = {
    status,
    statusText: status === 401 ? 'Unauthorized' : 'Error',
    headers: {},
    config: config as never,
    data: {},
  };
  return error;
};

const installAdapter = (handler: (config: { url?: string }) => Promise<unknown>) => {
  const adapter = vi.fn(async (config: { url?: string }) => {
    const result = await handler(config);
    return {
      data: result,
      status: 200,
      statusText: 'OK',
      headers: {},
      config: config as never,
    };
  });
  (axiosInstance.defaults as { adapter?: unknown }).adapter = adapter as never;
  return adapter;
};

describe('apiClient auth token refresh on 401', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    (axiosInstance.defaults as { adapter?: unknown }).adapter = undefined;
  });

  it('retries the original request once with a fresh token after a 401', async () => {
    localStorage.setItem('refreshToken', 'refresh-token-1');

    let ordersCalls = 0;
    const adapter = installAdapter(async (config) => {
      if (config.url === '/api/v1/auth/refresh') {
        return { accessToken: 'fresh-access-token', refreshToken: 'refresh-token-2' };
      }
      ordersCalls += 1;
      if (ordersCalls === 1) {
        throw makeAxiosError(401, config);
      }
      return { ok: true };
    });

    const result = await axiosInstance.get('/api/v1/orders');

    expect(adapter).toHaveBeenCalledTimes(3);
    expect(localStorage.getItem('accessToken')).toBe('fresh-access-token');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-token-2');
    expect(result.data).toEqual({ ok: true });
  });

  it('clears the session when the refresh itself fails', async () => {
    localStorage.setItem('accessToken', 'expired');
    localStorage.setItem('token', 'expired');
    localStorage.setItem('refreshToken', 'refresh-token-1');
    localStorage.setItem('sporekart_user', '{}');

    const adapter = installAdapter(async (config) => {
      throw makeAxiosError(401, config);
    });

    await expect(axiosInstance.get('/api/v1/orders')).rejects.toBeDefined();

    expect(adapter).toHaveBeenCalledTimes(2);
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('sporekart_user')).toBeNull();
  });

  it('does not attempt refresh when no session exists', async () => {
    const adapter = installAdapter(async (config) => {
      throw makeAxiosError(401, config);
    });

    await expect(axiosInstance.get('/api/v1/orders')).rejects.toBeDefined();
    expect(adapter).toHaveBeenCalledTimes(1);
  });

  it('does not retry the refresh endpoint itself on a 401', async () => {
    localStorage.setItem('refreshToken', 'refresh-token-1');

    const adapter = installAdapter(async (config) => {
      throw makeAxiosError(401, config);
    });

    await expect(axiosInstance.get('/api/v1/auth/refresh')).rejects.toBeDefined();
    expect(adapter).toHaveBeenCalledTimes(1);
  });
});