export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export interface ApiErrorDetails {
  code: string;
  message: string;
  timestamp: string;
  path: string;
}

export interface ApiErrorResponse {
  success: false;
  error: ApiErrorDetails;
}

export interface HealthStatusData {
  status: string;
}

export interface VersionInfoData {
  appName: string;
  version: string;
}
