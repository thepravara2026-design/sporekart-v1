import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';
import { ApiResponse } from '../types/api';

export interface CartItemDto {
  id: string;
  productId: string;
  variantId?: string | null;
  sku: string;
  productName: string;
  variantName?: string | null;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartDto {
  id: string;
  customerId: string;
  status: string;
  currency: string;
  subtotal: number;
  itemCount: number;
  items: CartItemDto[];
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
  /** Optional destination address used for shipping estimation. */
  destinationAddress?: string;
  couponCode?: string;
}

/** A single server-calculated line in a checkout preview. */
export interface CheckoutLineResponse {
  cartItemId: string;
  productId: string;
  sku: string;
  productName: string;
  quantity: number;
  cartUnitPrice: number;
  authoritativeUnitPrice: number;
  priceChanged: boolean;
  lineSubtotal: number;
  discountAmount: number;
  taxAmount: number;
  lineTotal: number;
}

/** Server-authoritative price breakdown for a checkout preview. */
export interface PriceBreakdownResponse {
  subtotal: number;
  discountTotal: number;
  taxTotal: number;
  shippingFee: number;
  grandTotal: number;
  currency: string;
}

/** A warning issued during checkout preview calculation (e.g. PRICE_CHANGED). */
export interface CheckoutWarningResponse {
  type: string;
  productId: string;
  message: string;
}

export interface CheckoutPreviewResponse {
  previewId: string;
  cartId: string;
  customerId: string;
  currency: string;
  items: CheckoutLineResponse[];
  breakdown: PriceBreakdownResponse;
  warnings: CheckoutWarningResponse[];
  generatedAt: string;
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
