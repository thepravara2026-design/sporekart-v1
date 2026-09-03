import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiResponse, HealthStatusData, VersionInfoData, ApiErrorResponse } from '../types/api';
import { ApiError } from './apiError';
import { ENDPOINTS } from './endpoints';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

export const axiosInstance: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken') || localStorage.getItem('token');
    if (token && config.headers && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

interface RetriableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

let refreshPromise: Promise<string> | null = null;

const getRefreshToken = (): string | null =>
  localStorage.getItem('refreshToken');

const persistRefreshedSession = (accessToken: string, refreshToken?: string): void => {
  localStorage.setItem('accessToken', accessToken);
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken);
  }
};

const clearSession = (): void => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('sporekart_user');
};

const performRefresh = async (): Promise<string> => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }
  const response = await axiosInstance.post<{ accessToken: string; refreshToken?: string }>(
    ENDPOINTS.AUTH_REFRESH,
    { refreshToken },
    { headers: { 'Content-Type': 'application/json' } }
  );
  if (!response.data?.accessToken) {
    throw new Error('Refresh response missing access token');
  }
  persistRefreshedSession(response.data.accessToken, response.data.refreshToken);
  return response.data.accessToken;
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorResponse>) => {
    const original = error.config as RetriableConfig | undefined;
    const status = error.response?.status;

    if (status !== 401 || !original || original._retry || original.url === ENDPOINTS.AUTH_REFRESH) {
      return Promise.reject(ApiError.fromAxiosError(error));
    }

    const hasSession = Boolean(getRefreshToken());
    if (!hasSession) {
      return Promise.reject(ApiError.fromAxiosError(error));
    }

    try {
      refreshPromise = refreshPromise ?? performRefresh();
      const newToken = await refreshPromise;
      refreshPromise = null;

      original._retry = true;
      original.headers = original.headers ?? {};
      original.headers.Authorization = `Bearer ${newToken}`;
      return axiosInstance(original);
    } catch {
      refreshPromise = null;
      clearSession();
      return Promise.reject(ApiError.fromAxiosError(error));
    }
  }
);

export const apiClient = {
  getHealth: async (): Promise<ApiResponse<HealthStatusData>> => {
    const response = await axiosInstance.get<ApiResponse<HealthStatusData>>(ENDPOINTS.HEALTH);
    return response.data;
  },

  getVersion: async (): Promise<ApiResponse<VersionInfoData>> => {
    const response = await axiosInstance.get<ApiResponse<VersionInfoData>>(ENDPOINTS.VERSION);
    return response.data;
  },
};
