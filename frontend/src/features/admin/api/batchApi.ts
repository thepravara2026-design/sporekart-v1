import { axiosInstance } from '../../../services/apiClient';
import { PageResponse } from '../../../types/api';

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

export const cancelBatch = async (id: string, reason?: string): Promise<BatchDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/batches/${id}/cancel`, { reason });
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
  enrollmentCode?: string;
  batchId: string;
  batchCode?: string;
  traineeId: string;
  status: 'PENDING' | 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED' | 'COMPLETED';
  priceAmount?: number;
  currency?: string;
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
  batchCode?: string;
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

export interface AdminTrainingDashboardDto {
  activeProgramsCount: number;
  upcomingBatchesCount: number;
  totalConfiguredCapacity: number;
  totalOccupiedSeats: number;
  totalRemainingSeats: number;
  activeDemandCount: number;
  totalEnrollmentsCount: number;
  paymentPendingEnrollmentsCount: number;
  paymentFailedEnrollmentsCount: number;
  paymentVerifiedExceptionsCount: number;
  batchesApproachingFullCount: number;
  generatedAt: string;
}

export const fetchAdminDashboard = async (): Promise<AdminTrainingDashboardDto> => {
  const response = await axiosInstance.get('/api/v1/admin/training/dashboard');
  return response.data.data;
};

export const fetchAdminGlobalEnrollments = async (
  params?: { batchId?: string; status?: string; search?: string; page?: number; size?: number; sort?: string }
): Promise<PageResponse<EnrollmentLifecycleDto>> => {
  const response = await axiosInstance.get('/api/v1/admin/training/enrollments', { params });
  return response.data.data;
};

export const fetchAdminPaymentExceptions = async (
  params?: { page?: number; size?: number }
): Promise<PageResponse<TrainingPaymentStatusDto>> => {
  const response = await axiosInstance.get('/api/v1/admin/training/exceptions', { params });
  return response.data.data;
};

export const fetchAdminGlobalDemands = async (
  params?: { batchId?: string; status?: string; search?: string; page?: number; size?: number; sort?: string }
): Promise<PageResponse<DemandDto>> => {
  const response = await axiosInstance.get('/api/v1/admin/training/demand', { params });
  return response.data.data;
};

export interface TraineeDashboardDto {
  upcomingEnrollmentsCount: number;
  activeEnrollmentsCount: number;
  completedEnrollmentsCount: number;
  pendingEnrollmentsCount: number;
  activeDemandRequestsCount: number;
  nextUpcomingSessionTitle?: string;
  nextUpcomingBatchCode?: string;
  nextUpcomingStartDate?: string;
  nextUpcomingDeliveryMode?: string;
  nextUpcomingVenueOrMeeting?: string;
  generatedAt: string;
}

export interface TraineeEnrollmentDetailDto {
  id: string;
  enrollmentCode: string;
  batchId: string;
  programId: string;
  programTitle: string;
  programCategory: string;
  batchCode: string;
  deliveryMode: string;
  venueInfo?: string;
  meetingUrl?: string;
  timezone: string;
  schedules: ScheduleDto[];
  enrollmentStatus: string;
  paymentStatusSummary: string;
  priceAmount: number;
  currency: string;
  paymentReference?: string;
  enrolledAt?: string;
  confirmedAt?: string;
  activatedAt?: string;
  completedAt?: string;
  createdAt: string;
}

export const fetchTraineeDashboard = async (): Promise<TraineeDashboardDto> => {
  const response = await axiosInstance.get('/api/v1/trainee/training/dashboard');
  return response.data.data;
};

export const fetchTraineeUpcomingTraining = async (
  params?: { page?: number; size?: number }
): Promise<PageResponse<EnrollmentDto>> => {
  const response = await axiosInstance.get('/api/v1/trainee/training/upcoming', { params });
  return response.data.data;
};

export const fetchTraineeEnrollmentDetail = async (
  enrollmentId: string
): Promise<TraineeEnrollmentDetailDto> => {
  const response = await axiosInstance.get(`/api/v1/trainee/training/enrollments/${enrollmentId}`);
  return response.data.data;
};

export const cancelMyEnrollment = async (
  enrollmentId: string,
  reason?: string
): Promise<EnrollmentDto> => {
  const response = await axiosInstance.post(`/api/v1/trainee/training/enrollments/${enrollmentId}/cancel`, { reason });
  return response.data.data;
};

export const rescheduleMyEnrollment = async (
  enrollmentId: string,
  targetBatchId: string,
  reason?: string
): Promise<EnrollmentDto> => {
  const response = await axiosInstance.post(`/api/v1/trainee/training/enrollments/${enrollmentId}/reschedule`, {
    targetBatchId,
    reason,
  });
  return response.data.data;
};

export const cancelAdminEnrollment = async (
  enrollmentId: string,
  reason?: string
): Promise<EnrollmentDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/training/enrollments/${enrollmentId}/cancel`, { reason });
  return response.data.data;
};

export const rescheduleAdminEnrollment = async (
  enrollmentId: string,
  targetBatchId: string,
  reason?: string
): Promise<EnrollmentDto> => {
  const response = await axiosInstance.post(`/api/v1/admin/training/enrollments/${enrollmentId}/reschedule`, {
    targetBatchId,
    reason,
  });
  return response.data.data;
};

export interface NotificationDto {
  id: string;
  eventType: string;
  channel: string;
  subject: string;
  body: string;
  status: string;
  priority: string;
  read: boolean;
  createdAt: string;
  deliveredAt?: string;
}

export const fetchTraineeNotifications = async (
  params?: { page?: number; size?: number }
): Promise<PageResponse<NotificationDto>> => {
  const response = await axiosInstance.get('/api/v1/notifications/in-app', { params });
  return response.data.data;
};

export const fetchUnreadNotificationCount = async (): Promise<number> => {
  const response = await axiosInstance.get('/api/v1/notifications/unread-count');
  return response.data.data?.unreadCount || 0;
};

export const markNotificationRead = async (notificationId: string): Promise<NotificationDto> => {
  const response = await axiosInstance.post(`/api/v1/notifications/${notificationId}/read`);
  return response.data.data;
};

export const markAllNotificationsRead = async (): Promise<void> => {
  await axiosInstance.post('/api/v1/notifications/mark-all-read');
};

export interface AttendanceDto {
  id: string;
  enrollmentId: string;
  scheduleId: string;
  batchId: string;
  traineeId: string;
  status: 'PRESENT' | 'ABSENT' | 'EXCUSED' | 'LATE';
  notes?: string;
  markedBy: string;
  markedAt: string;
}

export interface AttendanceSummaryDto {
  enrollmentId: string;
  batchId: string;
  traineeId: string;
  totalSessions: number;
  attendedSessions: number;
  attendancePercentage: number;
  sessionRecords: AttendanceDto[];
}

export interface CertificateDto {
  id: string;
  certificateNumber: string;
  verificationCode: string;
  enrollmentId: string;
  traineeId: string;
  traineeName: string;
  programId: string;
  programTitle: string;
  batchId: string;
  batchCode: string;
  issuedAt: string;
  completionDate: string;
  issuerSignature: string;
  revoked: boolean;
}

export interface VerificationResultDto {
  valid: boolean;
  verificationCode: string;
  certificateNumber?: string;
  traineeName?: string;
  programTitle?: string;
  batchCode?: string;
  completionDate?: string;
  issuerSignature?: string;
  statusMessage: string;
}

export const fetchSessionAttendanceRoster = async (
  batchId: string,
  scheduleId: string
): Promise<AttendanceDto[]> => {
  const response = await axiosInstance.get(`/api/v1/admin/batches/${batchId}/schedules/${scheduleId}/attendance`);
  return response.data.data;
};

export const bulkMarkAttendance = async (
  batchId: string,
  scheduleId: string,
  items: { enrollmentId: string; status: 'PRESENT' | 'ABSENT' | 'EXCUSED' | 'LATE'; notes?: string }[]
): Promise<AttendanceDto[]> => {
  const response = await axiosInstance.post(`/api/v1/admin/batches/${batchId}/schedules/${scheduleId}/attendance`, { items });
  return response.data.data;
};

export const evaluateBatchCompletion = async (
  batchId: string,
  minAttendancePercentage?: number
): Promise<any> => {
  const response = await axiosInstance.post(`/api/v1/admin/batches/${batchId}/evaluate-completion`, { minAttendancePercentage });
  return response.data.data;
};

export const fetchMyAttendanceSummary = async (
  enrollmentId: string
): Promise<AttendanceSummaryDto> => {
  const response = await axiosInstance.get(`/api/v1/trainee/training/enrollments/${enrollmentId}/attendance`);
  return response.data.data;
};

export const fetchMyCertificates = async (): Promise<CertificateDto[]> => {
  const response = await axiosInstance.get('/api/v1/trainee/training/certificates');
  return response.data.data;
};

export const fetchMyCertificateDetail = async (certificateId: string): Promise<CertificateDto> => {
  const response = await axiosInstance.get(`/api/v1/trainee/training/certificates/${certificateId}`);
  return response.data.data;
};

export const verifyCertificatePublic = async (verificationCode: string): Promise<VerificationResultDto> => {
  const response = await axiosInstance.get(`/api/v1/certificates/verify/${verificationCode}`);
  return response.data.data;
};

export interface ExecutiveOverviewDto {
  activeProgramsCount: number;
  totalBatches: number;
  activeBatches: number;
  fullBatches: number;
  totalCapacitySeats: number;
  totalOccupiedSeats: number;
  totalAvailableSeats: number;
  seatUtilizationPercentage: number;
  totalEnrollments: number;
  confirmedEnrollments: number;
  pendingPaymentEnrollments: number;
  cancelledEnrollments: number;
  completedEnrollments: number;
  grossRevenue: number;
  totalRefunds: number;
  netRevenue: number;
  certificatesIssuedCount: number;
}

export interface BatchUtilizationReportItemDto {
  batchId: string;
  programId: string;
  batchCode: string;
  status: string;
  totalSeats: number;
  occupiedSeats: number;
  availableSeats: number;
  utilizationPercentage: number;
}

export interface FailedNotificationItemDto {
  id: string;
  eventType: string;
  channel: string;
  recipient: string;
  errorMessage?: string;
  createdAt: string;
}

export interface NotificationOperationalReportDto {
  totalNotifications: number;
  delivered: number;
  failed: number;
  pending: number;
  failedItems: FailedNotificationItemDto[];
}

export interface AuditLogItemDto {
  id: string;
  enrollmentId: string;
  fromStatus: string;
  toStatus: string;
  reason?: string;
  actor: string;
  timestamp: string;
}

export interface OperationalExceptionItemDto {
  exceptionId: string;
  type: string;
  description: string;
  severity: string;
  resourceId: string;
  resourceType: string;
}

export const fetchExecutiveOverview = async (): Promise<ExecutiveOverviewDto> => {
  const response = await axiosInstance.get('/api/v1/admin/training/reports/overview');
  return response.data.data;
};

export const fetchBatchUtilizationReport = async (programId?: string): Promise<BatchUtilizationReportItemDto[]> => {
  const response = await axiosInstance.get('/api/v1/admin/training/reports/batches', { params: { programId } });
  return response.data.data;
};

export const fetchNotificationReport = async (): Promise<NotificationOperationalReportDto> => {
  const response = await axiosInstance.get('/api/v1/admin/training/reports/notifications');
  return response.data.data;
};

export const fetchAuditHistory = async (params?: { actor?: string; enrollmentId?: string }): Promise<AuditLogItemDto[]> => {
  const response = await axiosInstance.get('/api/v1/admin/training/reports/audit', { params });
  return response.data.data;
};

export const fetchOperationalExceptions = async (): Promise<OperationalExceptionItemDto[]> => {
  const response = await axiosInstance.get('/api/v1/admin/training/reports/exceptions');
  return response.data.data;
};

export const retryFailedNotificationControl = async (notificationId: string): Promise<any> => {
  const response = await axiosInstance.post(`/api/v1/admin/training/reports/controls/retry-notification/${notificationId}`);
  return response.data.data;
};

export const retryCertificateGenerationControl = async (enrollmentId: string): Promise<any> => {
  const response = await axiosInstance.post(`/api/v1/admin/training/reports/controls/retry-certificate/${enrollmentId}`);
  return response.data.data;
};






