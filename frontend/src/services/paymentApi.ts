import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';
import { ApiResponse } from '../types/api';

export interface PaymentCheckoutDto {
  paymentId: string;
  paymentReference: string;
  attemptId: string;
  attemptReference: string;
  orderId: string;
  amount: number;
  currency: string;
  provider: string;
  providerOrderId: string;
  keyId: string;
}

export interface PaymentAttemptDto {
  id: string;
  paymentId: string;
  attemptReference: string;
  provider: string;
  providerOrderId?: string | null;
  providerPaymentId?: string | null;
  status: string;
  amount: number;
  currency: string;
  failureCode?: string | null;
  failureReason?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentDto {
  id: string;
  paymentReference: string;
  orderId: string;
  customerId: string;
  amount: number;
  currency: string;
  status: string;
  provider: string;
  activeAttemptId?: string | null;
  attempts: PaymentAttemptDto[];
  createdAt: string;
  updatedAt: string;
}

export interface PaymentVerificationCommand {
  paymentReference: string;
  providerOrderId: string;
  providerPaymentId: string;
  providerSignature: string;
}

export const paymentApi = {
  initiatePayment: async (orderId: string, paymentMethod?: string): Promise<ApiResponse<PaymentCheckoutDto>> => {
    const response = await axiosInstance.post<ApiResponse<PaymentCheckoutDto>>(ENDPOINTS.PAYMENTS, {
      orderId,
      ...(paymentMethod ? { paymentMethod } : {}),
    });
    return response.data;
  },

  verifyPayment: async (command: PaymentVerificationCommand): Promise<ApiResponse<PaymentDto>> => {
    const response = await axiosInstance.post<ApiResponse<PaymentDto>>(ENDPOINTS.PAYMENT_VERIFY, command);
    return response.data;
  },

  getPaymentByReference: async (paymentReference: string): Promise<ApiResponse<PaymentDto>> => {
    const response = await axiosInstance.get<ApiResponse<PaymentDto>>(ENDPOINTS.PAYMENT_BY_REFERENCE(paymentReference));
    return response.data;
  },
};