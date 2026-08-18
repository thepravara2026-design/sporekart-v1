import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';
import { ApiResponse } from '../types/api';

export interface CartItemDto {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  unitPrice: number;
  quantity: number;
  lineSubtotal: number;
}

export interface CartDto {
  id: string;
  customerId: string;
  items: CartItemDto[];
  totalQuantity: number;
  subtotal: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface AddCartItemCommand {
  productId: string;
  quantity: number;
}

export interface UpdateCartItemCommand {
  quantity: number;
}

export interface CheckoutPreviewRequest {
  shippingAddressId?: string;
  couponCode?: string;
}

export interface CheckoutPreviewResponse {
  cartId: string;
  itemSubtotal: number;
  discountAmount: number;
  shippingFee: number;
  taxAmount: number;
  grandTotal: number;
  currency: string;
  itemsCount: number;
}

export const cartApi = {
  getCart: async (): Promise<ApiResponse<CartDto>> => {
    const response = await axiosInstance.get<ApiResponse<CartDto>>(ENDPOINTS.CART);
    return response.data;
  },

  addItem: async (command: AddCartItemCommand): Promise<ApiResponse<CartDto>> => {
    const response = await axiosInstance.post<ApiResponse<CartDto>>(ENDPOINTS.CART_ITEMS, command);
    return response.data;
  },

  updateItemQuantity: async (itemId: string, command: UpdateCartItemCommand): Promise<ApiResponse<CartDto>> => {
    const response = await axiosInstance.patch<ApiResponse<CartDto>>(ENDPOINTS.CART_ITEM_BY_ID(itemId), command);
    return response.data;
  },

  removeItem: async (itemId: string): Promise<ApiResponse<CartDto>> => {
    const response = await axiosInstance.delete<ApiResponse<CartDto>>(ENDPOINTS.CART_ITEM_BY_ID(itemId));
    return response.data;
  },

  clearCart: async (): Promise<ApiResponse<CartDto>> => {
    const response = await axiosInstance.delete<ApiResponse<CartDto>>(ENDPOINTS.CART_ITEMS);
    return response.data;
  },

  generateCheckoutPreview: async (request?: CheckoutPreviewRequest): Promise<ApiResponse<CheckoutPreviewResponse>> => {
    const response = await axiosInstance.post<ApiResponse<CheckoutPreviewResponse>>(ENDPOINTS.CHECKOUT_PREVIEW, request);
    return response.data;
  }
};
