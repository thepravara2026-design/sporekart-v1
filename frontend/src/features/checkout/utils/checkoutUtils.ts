import { ApiError } from '../../../services/apiError';
import { CheckoutWarningResponse } from '../../../services/cartApi';
import { CHECKOUT_ERROR_CODES } from '../constants/checkoutConstants';
import { AddressDto } from '../../../services/orderApi';

/** Human-friendly, safe message for a checkout/order/payment failure. */
export const getCheckoutErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    if (error.code === CHECKOUT_ERROR_CODES.CART_EMPTY) {
      return 'Your cart is empty. Add items before proceeding to checkout.';
    }
    if (error.code === CHECKOUT_ERROR_CODES.PAYMENT_VERIFICATION_FAILED) {
      return 'Payment could not be verified. Please review your payment details and try again.';
    }
    if (error.code === CHECKOUT_ERROR_CODES.PAYMENT_PROVIDER_UNAVAILABLE) {
      return 'The payment provider is temporarily unavailable. Please try again shortly.';
    }
    if (error.code === CHECKOUT_ERROR_CODES.ORDER_NOT_FOUND) {
      return 'Order not found. It may have been placed on another account.';
    }
    if (error.code === CHECKOUT_ERROR_CODES.ORDER_NOT_PAYABLE) {
      return 'This order cannot be paid right now. It may have expired or been cancelled.';
    }
    if (error.status === 401) {
      return 'Authentication required. Please sign in to continue checkout.';
    }
    if (error.status === 409) {
      return 'This order was already placed. Please check your order history.';
    }
    return 'Unable to complete checkout. Please try again.';
  }
  if (error instanceof Error) {
    return 'Unable to complete checkout. Please try again.';
  }
  return 'Unable to complete checkout. Please try again.';
};

/** Formats a destination address string for the checkout preview request. */
export const formatDestinationAddress = (address: AddressDto): string => {
  const parts = [address.addressLine1, address.addressLine2, address.city, address.state, address.postalCode].filter(Boolean);
  return parts.join(', ');
};

/** Warning types emitted by the backend checkout preview (CheckoutWarning.WarningType). */
export const CHECKOUT_WARNING_TYPES = {
  PRICE_CHANGED: 'PRICE_CHANGED',
  ITEM_UNAVAILABLE: 'ITEM_UNAVAILABLE',
  STOCK_LIMITED: 'STOCK_LIMITED',
  GENERAL: 'GENERAL',
} as const;

/** True when any server-side checkout warning requires customer acknowledgement. */
export const hasCheckoutWarnings = (warnings: CheckoutWarningResponse[]): boolean => warnings.length > 0;