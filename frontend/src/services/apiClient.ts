import axios, { AxiosInstance } from 'axios';
import { ApiResponse, HealthStatusData, VersionInfoData } from '../types/api';
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

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    return Promise.reject(ApiError.fromAxiosError(error));
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
