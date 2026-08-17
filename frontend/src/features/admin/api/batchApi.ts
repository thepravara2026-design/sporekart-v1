import { axiosInstance } from '../../../services/apiClient';

export type BatchStatusType = 'PLANNED' | 'SCHEDULED' | 'ACTIVE' | 'FULL' | 'COMPLETED' | 'CANCELLED';
export type DeliveryModeType = 'ONLINE' | 'OFFLINE' | 'HYBRID';

export interface ScheduleDto {
  id: string;
  batchId: string;
  title: string;
  scheduledAt: string;
  durationMinutes: number;
  location?: string;
}

export interface BatchDto {
  id: string;
  programId: string;
  batchCode: string;
  startDate: string;
  endDate: string;
  totalCapacity: number;
  occupiedSeats: number;
  status: BatchStatusType;
  deliveryMode: DeliveryModeType;
  venueInfo?: string;
  meetingUrl?: string;
  timezone: string;
  createdBy?: string;
  updatedBy?: string;
  schedules: ScheduleDto[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateBatchPayload {
  programId: string;
  batchCode: string;
  startDate: string;
  endDate: string;
  totalCapacity: number;
  deliveryMode?: DeliveryModeType;
  venueInfo?: string;
  meetingUrl?: string;
  timezone?: string;
}

export interface UpdateBatchPayload {
  programId?: string;
  startDate?: string;
  endDate?: string;
  timezone?: string;
  deliveryMode?: DeliveryModeType;
  venueInfo?: string;
  meetingUrl?: string;
}

export interface AddSchedulePayload {
  title: string;
  scheduledAt: string;
  durationMinutes: number;
  location?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const fetchAdminBatches = async (
  params?: { programId?: string; status?: string; deliveryMode?: string; fromDate?: string; toDate?: string; page?: number; size?: number }
): Promise<PageResponse<BatchDto>> => {
  const response = await axiosInstance.get('/api/v1/admin/batches', { params });
  return response.data.data;
};

export const createBatch = async (
  payload: CreateBatchPayload
): Promise<BatchDto> => {
  const response = await axiosInstance.post('/api/v1/admin/batches', payload);
  return response.data.data;
};

export const updateBatch = async (
  id: string,
  payload: UpdateBatchPayload
): Promise<BatchDto> => {
  const response = await axiosInstance.put(`/api/v1/admin/batches/${id}`, payload);
  return response.data.data;
};

export const activateBatch = async (id: string): Promise<BatchDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/batches/${id}/activate`);
  return response.data.data;
};

export const deactivateBatch = async (id: string): Promise<BatchDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/batches/${id}/deactivate`);
  return response.data.data;
};

export interface CapacityDto {
  batchId: string;
  batchCode: string;
  totalCapacity: number;
  occupiedSeats: number;
  availableSeats: number;
  full: boolean;
  status: BatchStatusType;
}

export const updateBatchCapacity = async (id: string, capacity: number): Promise<CapacityDto> => {
  const response = await axiosInstance.patch(`/api/v1/admin/batches/${id}/capacity`, { capacity });
  return response.data.data;
};

