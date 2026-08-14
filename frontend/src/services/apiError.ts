import { ApiErrorResponse } from '../types/api';
import { AxiosError } from 'axios';

export class ApiError extends Error {
  public code: string;
  public status?: number;
  public path?: string;
  public timestamp?: string;

  constructor(message: string, code = 'UNKNOWN_ERROR', status?: number, path?: string, timestamp?: string) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
    this.path = path;
    this.timestamp = timestamp;
  }

  public static fromAxiosError(error: AxiosError<ApiErrorResponse>): ApiError {
    if (error.response?.data?.error) {
      const { code, message, path, timestamp } = error.response.data.error;
      return new ApiError(message, code, error.response.status, path, timestamp);
    }
    if (error.response) {
      return new ApiError(`HTTP Error ${error.response.status}`, `HTTP_${error.response.status}`, error.response.status);
    }
    if (error.request) {
      return new ApiError('Backend server is unavailable or network is disconnected.', 'NETWORK_ERROR');
    }
    return new ApiError(error.message || 'An unexpected error occurred.', 'CLIENT_ERROR');
  }
}
