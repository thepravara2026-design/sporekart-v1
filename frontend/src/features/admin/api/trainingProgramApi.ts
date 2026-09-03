import { axiosInstance } from '../../../services/apiClient';
import { PageResponse } from '../../../types/api';

export interface TrainingProgramDto {
  id: string;
  slug: string;
  title: string;
  description?: string;
  category: string;
  durationHours: number;
  status: 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
  priceAmount: number;
  currency: string;
  createdBy?: string;
  updatedBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTrainingProgramPayload {
  title: string;
  description?: string;
  category?: string;
  durationHours?: number;
  priceAmount: number;
  currency?: string;
}

export interface UpdateTrainingProgramPayload {
  title?: string;
  description?: string;
  category?: string;
  durationHours?: number;
  priceAmount?: number;
  currency?: string;
}

export const fetchAdminTrainingPrograms = async (
  params?: { search?: string; status?: string; category?: string; page?: number; size?: number }
): Promise<PageResponse<TrainingProgramDto>> => {
  const response = await axiosInstance.get('/api/v1/admin/training-programs', { params });
  return response.data.data;
};

export const createTrainingProgram = async (
  payload: CreateTrainingProgramPayload
): Promise<TrainingProgramDto> => {
  const response = await axiosInstance.post('/api/v1/admin/training-programs', payload);
  return response.data.data;
};

export const updateTrainingProgram = async (
  id: string,
  payload: UpdateTrainingProgramPayload
): Promise<TrainingProgramDto> => {
  const response = await axiosInstance.put(`/api/v1/admin/training-programs/${id}`, payload);
  return response.data.data;
};

export const activateTrainingProgram = async (id: string): Promise<TrainingProgramDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/training-programs/${id}/activate`);
  return response.data.data;
};

export const deactivateTrainingProgram = async (id: string): Promise<TrainingProgramDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/training-programs/${id}/deactivate`);
  return response.data.data;
};
