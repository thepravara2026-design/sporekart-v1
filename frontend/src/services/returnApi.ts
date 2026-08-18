import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';

export interface ReturnItemDto {
  id: string;
  orderItemId: string;
  productId: string;
  sku: string;
  productNameSnapshot: string;
  requestedQuantity: number;
  approvedQuantity: number;
  receivedQuantity: number;
  acceptedQuantity: number;
  rejectedQuantity: number;
  unitPrice: number;
  refundAmount: number;
  reasonCode: string;
}

export interface ReturnStatusHistoryDto {
  id?: string;
  previousStatus?: string | null;
  newStatus: string;
  reason?: string | null;
  actorType: string;
  actorId?: string | null;
  correlationId?: string | null;
  createdAt?: string;
  timestamp?: string;
}

export interface ReturnInspectionDto {
  outcome?: string;
  notes?: string;
  inspectedAt?: string;
  inspectorId?: string;
}

export interface RefundRecordDto {
  id?: string;
  refundReference: string;
  amount: number;
  currency: string;
  status: string;
  failureReason?: string;
  provider?: string;
  providerRefundId?: string;
}

export interface ReturnDto {
  id: string;
  returnReference: string;
  orderId: string;
  orderReference: string;
  customerId: string;
  status: string;
  reasonCode: string;
  reasonDescription?: string;
  evidenceUrls?: string;
  policyVersion?: string;
  requestedAt: string;
  approvedAt?: string;
  receivedAt?: string;
  inspectedAt?: string;
  completedAt?: string;
  reverseShipmentId?: string | null;
  version?: number;
  totalRefundableAmount: number;
  items: ReturnItemDto[];
  statusHistory: ReturnStatusHistoryDto[];
  inspection?: ReturnInspectionDto | null;
  refundRecord?: RefundRecordDto | null;
}

export interface ItemEligibilityDto {
  orderItemId: string;
  productId: string;
  sku: string;
  productName: string;
  orderedQuantity: number;
  previouslyReturnedQuantity: number;
  returnableQuantity: number;
  isReturnable: boolean;
  reasonCode?: string;
}

export interface ReturnEligibilityDto {
  orderId: string;
  orderReference: string;
  eligible: boolean;
  ineligibilityReason?: string;
  deliveryTimestamp?: string;
  returnDeadline?: string;
  items: ItemEligibilityDto[];
}

export interface CreateReturnCommand {
  reasonCode: string;
  reasonDescription?: string;
  evidenceUrls?: string;
  items: Array<{ orderItemId: string; quantity: number; itemReasonCode?: string }>;
}

export const returnApi = {
  checkEligibility: async (orderRef: string): Promise<ReturnEligibilityDto> => {
    const response = await axiosInstance.get<ReturnEligibilityDto>(ENDPOINTS.RETURNS_ELIGIBILITY(orderRef));
    return response.data;
  },

  createReturn: async (orderRef: string, payload: CreateReturnCommand): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(ENDPOINTS.RETURNS_CREATE(orderRef), payload);
    return response.data;
  },

  getReturnByReference: async (returnRef: string): Promise<ReturnDto> => {
    const response = await axiosInstance.get<ReturnDto>(ENDPOINTS.RETURN_BY_REF(returnRef));
    return response.data;
  },

  listCustomerReturns: async (): Promise<ReturnDto[]> => {
    const response = await axiosInstance.get<ReturnDto[]>(ENDPOINTS.RETURNS_CUSTOMER);
    return response.data;
  },

  cancelReturn: async (returnRef: string): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(`/api/v1/returns/${returnRef}/cancel`, {});
    return response.data;
  },

  listAdminReturns: async (status?: string, searchKey?: string, customerId?: string): Promise<ReturnDto[]> => {
    const response = await axiosInstance.get<ReturnDto[]>(ENDPOINTS.ADMIN_RETURNS, {
      params: { status, searchKey, customerId }
    });
    return response.data;
  },

  approveReturn: async (returnRef: string, notes?: string, adminId?: string): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(
      ENDPOINTS.ADMIN_RETURN_APPROVE(returnRef),
      { notes },
      { headers: adminId ? { 'X-Admin-Id': adminId } : undefined }
    );
    return response.data;
  },

  rejectReturn: async (returnRef: string, reason?: string, adminId?: string): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(
      ENDPOINTS.ADMIN_RETURN_REJECT(returnRef),
      { reason },
      { headers: adminId ? { 'X-Admin-Id': adminId } : undefined }
    );
    return response.data;
  },

  inspectReturn: async (
    returnRef: string,
    inspectionPayload: { outcome: string; notes?: string; inspectorId?: string },
    adminId?: string
  ): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(
      ENDPOINTS.ADMIN_RETURN_INSPECT(returnRef),
      inspectionPayload,
      { headers: adminId ? { 'X-Admin-Id': adminId } : undefined }
    );
    return response.data;
  },

  retryRefund: async (returnRef: string): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(`/api/v1/admin/returns/${returnRef}/refund/retry`);
    return response.data;
  },

  syncReturn: async (returnRef: string): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(`/api/v1/admin/returns/${returnRef}/sync`);
    return response.data;
  }
};
