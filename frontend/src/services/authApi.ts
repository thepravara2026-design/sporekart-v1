import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';

export interface RegisterRequestDto {
  email: string;
  password?: string;
  firstName?: string;
  lastName?: string;
}

export interface LoginRequestDto {
  email: string;
  password?: string;
}

export interface RefreshTokenRequestDto {
  refreshToken: string;
}

export interface AuthTokenResponseDto {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  userId: string;
  email: string;
  role: string;
  sessionId: string;
}

export interface UserProfileDto {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: string;
  status: string;
}

export interface ChangePasswordRequestDto {
  currentPassword?: string;
  newPassword?: string;
}

export interface UserSessionDto {
  sessionId: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
  lastAccessedAt: string;
}

export const authApi = {
  register: async (payload: RegisterRequestDto): Promise<UserProfileDto> => {
    const response = await axiosInstance.post<UserProfileDto>(ENDPOINTS.AUTH_REGISTER, payload);
    return response.data;
  },

  login: async (payload: LoginRequestDto): Promise<AuthTokenResponseDto> => {
    const response = await axiosInstance.post<AuthTokenResponseDto>(ENDPOINTS.AUTH_LOGIN, payload);
    if (response.data?.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
    }
    return response.data;
  },

  refreshToken: async (payload: RefreshTokenRequestDto): Promise<AuthTokenResponseDto> => {
    const response = await axiosInstance.post<AuthTokenResponseDto>(ENDPOINTS.AUTH_REFRESH, payload);
    if (response.data?.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
    }
    return response.data;
  },

  logout: async (): Promise<void> => {
    try {
      await axiosInstance.post(ENDPOINTS.AUTH_LOGOUT);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('token');
    }
  },

  logoutAll: async (): Promise<void> => {
    try {
      await axiosInstance.post(ENDPOINTS.AUTH_LOGOUT_ALL);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('token');
    }
  },

  getCurrentUser: async (): Promise<UserProfileDto> => {
    const response = await axiosInstance.get<UserProfileDto>(ENDPOINTS.AUTH_ME);
    return response.data;
  },

  changePassword: async (payload: ChangePasswordRequestDto): Promise<void> => {
    await axiosInstance.post(ENDPOINTS.AUTH_CHANGE_PASSWORD, payload);
  },

  getActiveSessions: async (): Promise<UserSessionDto[]> => {
    const response = await axiosInstance.get<UserSessionDto[]>(ENDPOINTS.AUTH_SESSIONS);
    return response.data;
  }
};
