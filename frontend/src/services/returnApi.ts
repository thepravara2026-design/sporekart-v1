import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';
import { ApiResponse } from '../types/api';

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
  previousStatus: string | null;
  newStatus: string;
  reason: string;
  actorType: string;
  actorId: string;
  timestamp: string;
}

export interface ReturnInspectionDto {
  id: string | null;
  inspectorId: string;
  outcome: 'ACCEPTED' | 'PARTIALLY_ACCEPTED' | 'RETURN_REJECTED' | 'QUARANTINED';
  notes: string;
  inspectedAt: string | null;
  itemInspections?: Array<{
    returnItemId: string;
    acceptedQuantity: number;
    rejectedQuantity: number;
  }>;
}

export interface RefundRecordDto {
  id: string;
  refundReference: string;
  returnId: string;
  returnReference: string;
  orderId: string;
  customerId: string;
  paymentId: string;
  paymentReference: string;
  provider: string;
  amount: number;
  currency: string;
  status: string;
  failureReason: string | null;
  providerRefundId: string | null;
  idempotencyKey: string;
  createdAt: string;
}

export interface ReturnDto {
  id: string;
  returnReference: string;
  orderId: string;
  orderReference: string;
  customerId: string;
  status: string;
  reasonCode: string;
  reasonDescription: string;
  evidenceUrls: string | null;
  policyVersion: string;
  requestedAt: string;
  approvedAt: string | null;
  receivedAt: string | null;
  inspectedAt: string | null;
  completedAt: string | null;
  reverseShipmentId?: string | null;
  totalRefundableAmount: number;
  items: ReturnItemDto[];
  statusHistory: ReturnStatusHistoryDto[];
  inspection: ReturnInspectionDto | null;
  refundRecord: RefundRecordDto | null;
}

export interface ItemEligibilityDto {
  orderItemId: string;
  productId: string;
  sku: string;
  productNameSnapshot: string;
  originalQuantity: number;
  previouslyReturnedQuantity: number;
  returnableQuantity: number;
  unitPrice: number;
  returnable: boolean;
  ineligibilityReason: string;
}

export interface ReturnEligibilityDto {
  eligible: boolean;
  ineligibilityReason: string;
  deliveryTimestamp: string | null;
  returnWindowExpiry: string | null;
  items: ItemEligibilityDto[];
}

export interface CreateReturnInput {
  reasonCode: string;
  reasonDescription: string;
  evidenceUrls?: string;
  items: Array<{
    orderItemId: string;
    quantity: number;
    itemReasonCode?: string;
  }>;
}

export const returnApi = {
  checkEligibility: async (orderRef: string, customerId: string = 'cust-101'): Promise<ReturnEligibilityDto> => {
    const res = await axiosInstance.get<ApiResponse<ReturnEligibilityDto>>(
      ENDPOINTS.RETURNS_ELIGIBILITY(orderRef),
      { headers: { 'X-Customer-Id': customerId } }
    );
    return res.data.data;
  },

  createReturn: async (orderRef: string, input: CreateReturnInput, customerId: string = 'cust-101'): Promise<ReturnDto> => {
    const res = await axiosInstance.post<ApiResponse<ReturnDto>>(
      ENDPOINTS.RETURNS_CREATE,
      input,
      {
        params: { orderReference: orderRef },
        headers: { 'X-Customer-Id': customerId }
      }
    );
    return res.data.data;
  },

  getReturnByReference: async (returnRef: string, customerId: string = 'cust-101'): Promise<ReturnDto> => {
    const res = await axiosInstance.get<ApiResponse<ReturnDto>>(
      ENDPOINTS.RETURN_BY_REF(returnRef),
      { headers: { 'X-Customer-Id': customerId } }
    );
    return res.data.data;
  },

  listCustomerReturns: async (customerId: string = 'cust-101'): Promise<ReturnDto[]> => {
    const res = await axiosInstance.get<ApiResponse<ReturnDto[]>>(
      ENDPOINTS.RETURNS_CUSTOMER,
      { headers: { 'X-Customer-Id': customerId } }
    );
    return res.data.data;
  },

  listAdminReturns: async (customerId?: string, orderRef?: string, status?: string): Promise<ReturnDto[]> => {
    const res = await axiosInstance.get<ApiResponse<ReturnDto[]>>(
      ENDPOINTS.ADMIN_RETURNS,
      { params: { customerId, orderReference: orderRef, status } }
    );
    return res.data.data;
  },

  approveReturn: async (returnRef: string, adminId: string = 'admin-1', notes?: string): Promise<ReturnDto> => {
    const res = await axiosInstance.post<ApiResponse<ReturnDto>>(
      ENDPOINTS.ADMIN_RETURN_APPROVE(returnRef),
      { notes },
      { headers: { 'X-Admin-Id': adminId } }
    );
    return res.data.data;
  },

  rejectReturn: async (returnRef: string, adminId: string = 'admin-1', reason: string = 'Policy violation'): Promise<ReturnDto> => {
    const res = await axiosInstance.post<ApiResponse<ReturnDto>>(
      ENDPOINTS.ADMIN_RETURN_REJECT(returnRef),
      { reason },
      { headers: { 'X-Admin-Id': adminId } }
    );
    return res.data.data;
  },

  createReverseShipment: async (returnRef: string, adminId: string = 'admin-1'): Promise<ReturnDto> => {
    const res = await axiosInstance.post<ApiResponse<ReturnDto>>(
      `/api/v1/admin/returns/${returnRef}/create-reverse-shipment`,
      {},
      { headers: { 'X-Admin-Id': adminId } }
    );
    return res.data.data;
  },

  reconcileReturn: async (returnRef: string, adminId: string = 'admin-1'): Promise<ReturnDto> => {
    const res = await axiosInstance.post<ApiResponse<ReturnDto>>(
      `/api/v1/admin/returns/${returnRef}/reconcile`,
      {},
      { headers: { 'X-Admin-Id': adminId } }
    );
    return res.data.data;
  },

  inspectReturn: async (returnRef: string, inspection: Partial<ReturnInspectionDto>, adminId: string = 'inspector-1'): Promise<ReturnDto> => {
    const res = await axiosInstance.post<ApiResponse<ReturnDto>>(
      ENDPOINTS.ADMIN_RETURN_INSPECT(returnRef),
      inspection,
      { headers: { 'X-Admin-Id': adminId } }
    );
    return res.data.data;
  },
};
