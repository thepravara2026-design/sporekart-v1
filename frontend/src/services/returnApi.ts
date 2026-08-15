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
  previousStatus?: string;
  newStatus: string;
  reason?: string;
  actorType: string;
  actorId?: string;
  timestamp: string;
  createdAt?: string;
}

export interface RefundRecordDto {
  id: string;
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
  totalRefundableAmount: number;
  requestedAt: string;
  approvedAt?: string;
  receivedAt?: string;
  inspectedAt?: string;
  completedAt?: string;
  items: ReturnItemDto[];
  statusHistory: ReturnStatusHistoryDto[];
  refundRecord?: RefundRecordDto;
}

export interface ItemEligibilityDto {
  orderItemId: string;
  sku: string;
  productNameSnapshot: string;
  unitPrice: number;
  originalQuantity: number;
  deliveredQuantity: number;
  returnedQuantity: number;
  returnableQuantity: number;
  returnable: boolean;
  ineligibilityReason?: string;
}

export interface ReturnEligibilityDto {
  eligible: boolean;
  ineligibilityReason?: string;
  itemEligibilities: ItemEligibilityDto[];
  items: ItemEligibilityDto[];
}

export const returnApi = {
  checkEligibility: async (orderRef: string, customerId: string): Promise<ReturnEligibilityDto> => {
    const response = await axiosInstance.get<ReturnEligibilityDto>(
      ENDPOINTS.RETURNS_ELIGIBILITY(orderRef),
      { headers: { 'X-Customer-Id': customerId } }
    );
    const data = response.data;
    if (data && data.itemEligibilities && !data.items) {
      data.items = data.itemEligibilities;
    }
    return data;
  },

  createReturn: async (
    orderRef: string,
    payload: {
      reasonCode: string;
      reasonDescription?: string;
      evidenceUrls?: string;
      items: { orderItemId: string; quantity: number }[];
    },
    customerId: string
  ): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(
      `/api/v1/orders/${orderRef}/returns`,
      payload,
      { headers: { 'X-Customer-Id': customerId } }
    );
    return response.data;
  },

  getReturnByReference: async (returnRef: string, customerId?: string): Promise<ReturnDto> => {
    const headers = customerId ? { 'X-Customer-Id': customerId } : undefined;
    const response = await axiosInstance.get<ReturnDto>(ENDPOINTS.RETURN_BY_REF(returnRef), { headers });
    return response.data;
  },

  listCustomerReturns: async (customerId: string): Promise<ReturnDto[]> => {
    const response = await axiosInstance.get<ReturnDto[]>(ENDPOINTS.RETURNS_CUSTOMER, {
      headers: { 'X-Customer-Id': customerId }
    });
    return response.data;
  },

  cancelReturn: async (returnRef: string, customerId: string): Promise<ReturnDto> => {
    const response = await axiosInstance.post<ReturnDto>(
      `/api/v1/returns/${returnRef}/cancel`,
      {},
      { headers: { 'X-Customer-Id': customerId } }
    );
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
