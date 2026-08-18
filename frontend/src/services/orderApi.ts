import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';
import { ApiResponse } from '../types/api';

export interface AddressDto {
  fullName: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string | null;
  city: string;
  state: string;
  postalCode: string;
  country?: string | null;
}

export interface CreateOrderCommand {
  shippingAddress: AddressDto;
  idempotencyKey?: string;
  customerNotes?: string;
}

export interface OrderItemDto {
  id: string;
  orderId: string;
  productId: string;
  variantId?: string | null;
  sku: string;
  productNameSnapshot: string;
  variantNameSnapshot?: string | null;
  unitPrice: number;
  quantity: number;
  discountAmount: number;
  taxAmount: number;
  lineSubtotal: number;
  lineTotal: number;
  createdAt: string;
}

export interface OrderDto {
  id: string;
  orderNumber: string;
  customerId: string;
  status: string;
  currency: string;
  subtotal: number;
  discountTotal: number;
  taxTotal: number;
  shippingFee: number;
  grandTotal: number;
  idempotencyKey?: string | null;
  shippingAddress?: AddressDto | null;
  customerNotes?: string | null;
  items: OrderItemDto[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderSummaryDto {
  id: string;
  orderNumber: string;
  customerId: string;
  status: string;
  currency: string;
  grandTotal: number;
  itemCount: number;
  createdAt: string;
}

export const orderApi = {
  createOrder: async (command: CreateOrderCommand): Promise<ApiResponse<OrderDto>> => {
    const response = await axiosInstance.post<ApiResponse<OrderDto>>(ENDPOINTS.ORDERS, command);
    return response.data;
  },

  getOrderByReference: async (orderReference: string): Promise<ApiResponse<OrderDto>> => {
    const response = await axiosInstance.get<ApiResponse<OrderDto>>(ENDPOINTS.ORDER_BY_REFERENCE(orderReference));
    return response.data;
  },

  getOrderHistory: async (page = 0, size = 10): Promise<ApiResponse<{ content: OrderSummaryDto[] }>> => {
    const response = await axiosInstance.get<ApiResponse<{ content: OrderSummaryDto[] }>>(ENDPOINTS.ORDERS, {
      params: { page, size },
    });
    return response.data;
  },
};