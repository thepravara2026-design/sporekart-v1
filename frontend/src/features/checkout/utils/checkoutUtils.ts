import { ApiError } from '../../../services/apiError';
import { CheckoutPreviewResponse, CheckoutWarningResponse } from '../../../services/cartApi';
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

/** Alert variant to surface a backend checkout warning with. */
export const getCheckoutWarningVariant = (type: string): 'warning' | 'error' | 'info' => {
  if (type === CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE) return 'error';
  if (type === CHECKOUT_WARNING_TYPES.PRICE_CHANGED) return 'warning';
  if (type === CHECKOUT_WARNING_TYPES.STOCK_LIMITED) return 'warning';
  return 'info';
};

/** Human title for a backend checkout warning. */
export const getCheckoutWarningTitle = (type: string): string => {
  switch (type) {
    case CHECKOUT_WARNING_TYPES.PRICE_CHANGED:
      return 'Prices have changed';
    case CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE:
      return 'Item no longer available';
    case CHECKOUT_WARNING_TYPES.STOCK_LIMITED:
      return 'Limited stock';
    default:
      return 'Please review';
  }
};

/**
 * A blocking warning prevents the order from being placed until the customer
 * acts (e.g. an item became unavailable). Price changes are surfaced but the
 * customer may proceed once they have reviewed the updated totals.
 */
export const isBlockingCheckoutWarning = (type: string): boolean =>
  type === CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE;

/**
 * True when a refreshed preview differs materially from the preview the
 * customer is reviewing (grand total, currency, or any blocking warning).
 * Used to prevent silent submission with stale totals.
 */
export const hasPreviewChanged = (
  previous: CheckoutPreviewResponse | null | undefined,
  next: CheckoutPreviewResponse
): boolean => {
  if (!previous) return false;
  if (previous.breakdown.grandTotal !== next.breakdown.grandTotal) return true;
  if (previous.breakdown.currency !== next.breakdown.currency) return true;
  const previousBlocking = previous.warnings.some((warning) => isBlockingCheckoutWarning(warning.type));
  const nextBlocking = next.warnings.some((warning) => isBlockingCheckoutWarning(warning.type));
  return previousBlocking !== nextBlocking;
};

/** One-line human-friendly summary of a shipping address (for review surfaces). */
export const formatAddressSummary = (address: AddressDto): string => {
  const parts = [address.fullName, address.addressLine1, address.addressLine2, address.city, address.state, address.postalCode, address.country]
    .map((part) => part?.trim())
    .filter(Boolean);
  return parts.join(', ');
};