/**
 * Checkout domain constants. The step order is the single source of truth for
 * the CheckoutPage multi-step flow and the CheckoutStepper component.
 */
export const CHECKOUT_STEP_LABELS = ['Shipping', 'Payment', 'Review & Place Order'] as const;

export type CheckoutStep = (typeof CHECKOUT_STEP_LABELS)[number];
export type CheckoutStepIndex = 0 | 1 | 2;

/** Supported payment methods presented in the checkout payment form. */
export const PAYMENT_METHODS = [
  { value: 'UPI', label: 'UPI', description: 'Instant bank transfer via UPI' },
  { value: 'CARD', label: 'Credit / Debit Card', description: 'Visa, Mastercard, RuPay' },
  { value: 'NET_BANKING', label: 'Net Banking', description: 'Bank account transfer' },
] as const;

export type PaymentMethod = (typeof PAYMENT_METHODS)[number]['value'];

/**
 * Backend error codes surfaced by the checkout, order, and payment APIs.
 * Mirrors the backend GlobalExceptionHandler error codes.
 */
export const CHECKOUT_ERROR_CODES = {
  CART_EMPTY: 'CHECKOUT_CART_EMPTY',
  NOT_ELIGIBLE: 'CHECKOUT_NOT_ELIGIBLE',
  CURRENCY_MISMATCH: 'CHECKOUT_CURRENCY_MISMATCH',
  TAX_CALCULATION_FAILED: 'CHECKOUT_TAX_CALCULATION_FAILED',
  SHIPPING_RATE_UNAVAILABLE: 'CHECKOUT_SHIPPING_RATE_UNAVAILABLE',
  ORDER_NOT_FOUND: 'ORDER_NOT_FOUND',
  ORDER_ACCESS_DENIED: 'ORDER_ACCESS_DENIED',
  ORDER_NOT_CANCELLABLE: 'ORDER_NOT_CANCELLABLE',
  ORDER_NOT_PAYABLE: 'ORDER_NOT_PAYABLE',
  PAYMENT_NOT_FOUND: 'PAYMENT_NOT_FOUND',
  PAYMENT_VERIFICATION_FAILED: 'PAYMENT_VERIFICATION_FAILED',
  PAYMENT_INVALID_STATE: 'PAYMENT_INVALID_STATE',
  PAYMENT_PROVIDER_UNAVAILABLE: 'PAYMENT_PROVIDER_UNAVAILABLE',
} as const;