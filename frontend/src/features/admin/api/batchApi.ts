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

export interface EnrollmentDto {
  id: string;
  batchId: string;
  traineeId: string;
  status: 'PENDING' | 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED' | 'COMPLETED';
  paymentReference?: string;
  enrolledAt: string;
  createdAt: string;
  updatedAt: string;
}

export const enrollInBatch = async (batchId: string, idempotencyKey?: string): Promise<EnrollmentDto> => {
  const headers: Record<string, string> = {};
  if (idempotencyKey) {
    headers['X-Idempotency-Key'] = idempotencyKey;
  }
  const response = await axiosInstance.post(`/api/v1/batches/${batchId}/enrollments`, { idempotencyKey }, { headers });
  return response.data.data;
};

export const fetchMyEnrollments = async (
  params?: { page?: number; size?: number; sort?: string }
): Promise<PageResponse<EnrollmentDto>> => {
  const response = await axiosInstance.get('/api/v1/me/enrollments', { params });
  return response.data.data;
};

export const fetchBatchEnrollmentsForAdmin = async (
  batchId: string,
  params?: { page?: number; size?: number }
): Promise<PageResponse<EnrollmentDto>> => {
  const response = await axiosInstance.get(`/api/v1/admin/batches/${batchId}/enrollments`, { params });
  return response.data.data;
};

export interface DemandDto {
  id: string;
  batchId: string;
  traineeId: string;
  status: 'ACTIVE' | 'RESOLVED' | 'WITHDRAWN' | 'EXPIRED';
  requestedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface BatchDemandSummaryDto {
  batchId: string;
  activeDemandCount: number;
}

export const requestBatchDemand = async (batchId: string): Promise<DemandDto> => {
  const response = await axiosInstance.post(`/api/v1/batches/${batchId}/demand`);
  return response.data.data;
};

export const fetchMyDemands = async (
  params?: { page?: number; size?: number; sort?: string }
): Promise<PageResponse<DemandDto>> => {
  const response = await axiosInstance.get('/api/v1/me/demands', { params });
  return response.data.data;
};

export const fetchBatchDemandForAdmin = async (
  batchId: string,
  params?: { page?: number; size?: number }
): Promise<PageResponse<DemandDto>> => {
  const response = await axiosInstance.get(`/api/v1/admin/batches/${batchId}/demand`, { params });
  return response.data.data;
};

export const fetchBatchDemandSummaryForAdmin = async (
  batchId: string
): Promise<BatchDemandSummaryDto> => {
  const response = await axiosInstance.get(`/api/v1/admin/batches/${batchId}/demand/summary`);
  return response.data.data;
};

export const withdrawDemand = async (demandId: string): Promise<DemandDto> => {
  const response = await axiosInstance.delete(`/api/v1/demands/${demandId}`);
  return response.data.data;
};

export interface TrainingPaymentOrderDto {
  trainingPaymentId: string;
  paymentId: string;
  paymentReference: string;
  batchId: string;
  amount: number;
  currency: string;
  provider: 'RAZORPAY' | 'MOCK';
  providerOrderId: string;
  keyId: string;
}

export interface TrainingPaymentStatusDto {
  trainingPaymentId: string;
  paymentId: string;
  batchId: string;
  traineeId: string;
  enrollmentId?: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'VERIFIED' | 'ENROLLMENT_CONFIRMED' | 'ENROLLMENT_PENDING' | 'FAILED' | 'EXPIRED';
  createdAt: string;
  updatedAt: string;
}

export interface VerifyTrainingPaymentPayload {
  paymentReference: string;
  providerOrderId: string;
  providerPaymentId: string;
  providerSignature: string;
}

export const initiateTrainingPaymentOrder = async (batchId: string): Promise<TrainingPaymentOrderDto> => {
  const response = await axiosInstance.post(`/api/v1/batches/${batchId}/enrollment/payment-order`);
  return response.data.data;
};

export const verifyTrainingPayment = async (
  batchId: string,
  payload: VerifyTrainingPaymentPayload
): Promise<TrainingPaymentStatusDto> => {
  const response = await axiosInstance.post(`/api/v1/batches/${batchId}/enrollment/payment-verify`, payload);
  return response.data.data;
};

export const fetchTrainingPaymentStatus = async (batchId: string): Promise<TrainingPaymentStatusDto> => {
  const response = await axiosInstance.get(`/api/v1/batches/${batchId}/enrollment/payment-status`);
  return response.data.data;
};

export interface EnrollmentLifecycleDto {
  id: string;
  enrollmentCode: string;
  batchId: string;
  traineeId: string;
  status: 'PENDING' | 'PAYMENT_PENDING' | 'PAYMENT_VERIFIED' | 'CONFIRMED' | 'ACTIVE' | 'COMPLETED' | 'WAITLISTED' | 'REJECTED' | 'PAYMENT_FAILED' | 'CANCELLED' | 'RESCHEDULED';
  paymentReference?: string;
  priceAmount: number;
  currency: string;
  enrolledAt?: string;
  confirmedAt?: string;
  activatedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EnrollmentHistoryDto {
  id: string;
  enrollmentId: string;
  fromStatus?: string;
  toStatus: string;
  reason?: string;
  actor: string;
  createdAt: string;
}

export const fetchMyEnrollmentDetails = async (enrollmentId: string): Promise<EnrollmentLifecycleDto> => {
  const response = await axiosInstance.get(`/api/v1/training/my-enrollments/${enrollmentId}`);
  return response.data.data;
};

export const fetchMyEnrollmentHistory = async (enrollmentId: string): Promise<EnrollmentHistoryDto[]> => {
  const response = await axiosInstance.get(`/api/v1/training/my-enrollments/${enrollmentId}/history`);
  return response.data.data;
};

export const fetchAdminEnrollmentDetails = async (enrollmentId: string): Promise<EnrollmentLifecycleDto> => {
  const response = await axiosInstance.get(`/api/v1/admin/training/enrollments/${enrollmentId}`);
  return response.data.data;
};

export const triggerEnrollmentRecovery = async (enrollmentId: string): Promise<EnrollmentLifecycleDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/training/enrollments/${enrollmentId}/recover`);
  return response.data.data;
};




