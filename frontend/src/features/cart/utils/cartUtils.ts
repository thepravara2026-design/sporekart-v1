import { ApiError } from '../../../services/apiError';
import { CartItemDto } from '../../../services/cartApi';

/**
 * True when a bearer token is available. Mirrors the central API client's
 * token source (localStorage) — this only gates cart queries; the API client
 * remains the single authority for attaching Authorization headers.
 */
export const isAuthenticated = (): boolean => {
  if (typeof window === 'undefined') return false;
  return Boolean(localStorage.getItem('accessToken') || localStorage.getItem('token'));
};

/** Backend error codes surfaced by the cart/checkout API. */
export const CART_ERROR_CODES = {
  ITEM_NOT_FOUND: 'CART_ITEM_NOT_FOUND',
  CART_NOT_FOUND: 'CART_NOT_FOUND',
  INVALID_QUANTITY: 'CART_INVALID_QUANTITY',
  NOT_MODIFIABLE: 'CART_NOT_MODIFIABLE',
  CONCURRENCY_CONFLICT: 'CART_CONCURRENCY_CONFLICT',
  PRODUCT_NOT_PURCHASABLE: 'CATALOG_PRODUCT_NOT_PURCHASABLE',
  PRODUCT_NOT_FOUND: 'CATALOG_PRODUCT_NOT_FOUND',
  CHECKOUT_CART_EMPTY: 'CHECKOUT_CART_EMPTY',
} as const;

/** Human-friendly, safe message for a cart operation failure (no stack traces). */
export const getCartErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    if (error.code === CART_ERROR_CODES.PRODUCT_NOT_PURCHASABLE) {
      return 'This product is no longer available for purchase. Please remove it to continue.';
    }
    if (error.code === CART_ERROR_CODES.ITEM_NOT_FOUND) {
      return 'This item is no longer in your cart. Your cart has been refreshed.';
    }
    if (error.code === CART_ERROR_CODES.INVALID_QUANTITY) {
      return 'Unable to update quantity. The requested quantity is not allowed.';
    }
    if (error.status === 401) {
      return 'Authentication required. Please sign in to manage your cart.';
    }
    return error.message || 'Unable to complete this cart operation. Please try again.';
  }
  if (error instanceof Error) {
    return 'Unable to complete this cart operation. Please try again.';
  }
  return 'Unable to complete this cart operation. Please try again.';
};

/** Sum of line quantities; only a display convenience — backend totals are authoritative. */
export const getCartUnitCount = (items: CartItemDto[]): number =>
  items.reduce((sum, item) => sum + item.quantity, 0);
