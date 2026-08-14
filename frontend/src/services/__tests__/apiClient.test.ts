import { apiClient, axiosInstance } from '../apiClient';
import { describe, it, expect, vi } from 'vitest';

describe('apiClient', () => {
  it('calls /api/v1/health endpoint', async () => {
    const mockHealth = { success: true, data: { status: 'UP' } };
    vi.spyOn(axiosInstance, 'get').mockResolvedValueOnce({ data: mockHealth });

    const result = await apiClient.getHealth();
    expect(axiosInstance.get).toHaveBeenCalledWith('/api/v1/health');
    expect(result.data.status).toBe('UP');
  });

  it('calls /api/v1/version endpoint', async () => {
    const mockVersion = { success: true, data: { appName: 'sporekart', version: '0.1.0' } };
    vi.spyOn(axiosInstance, 'get').mockResolvedValueOnce({ data: mockVersion });

    const result = await apiClient.getVersion();
    expect(axiosInstance.get).toHaveBeenCalledWith('/api/v1/version');
    expect(result.data.version).toBe('0.1.0');
  });
});
