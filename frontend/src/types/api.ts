export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export interface ApiErrorDetails {
  code: string;
  message: string;
  timestamp: string;
  path?: string;
  requestId?: string;
}

export interface ApiErrorResponse {
  success: false;
  error: ApiErrorDetails;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface HealthStatusData {
  status: string;
}

export interface VersionInfoData {
  appName: string;
  version: string;
}
