import {
  CartDto,
  CartItemDto,
  AddCartItemCommand,
  UpdateCartItemCommand,
  CheckoutPreviewRequest,
  CheckoutPreviewResponse,
} from '../../../services/cartApi';
import { ApiResponse } from '../../../types/api';

export type {
  CartDto,
  CartItemDto,
  AddCartItemCommand,
  UpdateCartItemCommand,
  CheckoutPreviewRequest,
  CheckoutPreviewResponse,
  ApiResponse,
};

/**
 * Authenticated customer cart query result. `itemCount` is the authoritative
 * total quantity of units across all lines, provided by the backend.
 */
export interface CartQueryResult {
  cart: CartDto;
}
