import { CheckoutPreviewResponse } from '../../../services/cartApi';

/**
 * A single shipping option derived from the backend checkout preview. The
 * backend computes the only shipping method (flat rate / free above threshold);
 * the frontend never invents methods or fees.
 */
export interface ShippingOption {
  id: string;
  method: string;
  description: string;
  fee: number;
  currency: string;
}

/**
 * Result of refreshing the checkout preview immediately before the final
 * order submission. A changed grandTotal (or a new blocking warning) means the
 * customer must review the updated totals before placing the order.
 */
export interface CheckoutRevalidation {
  preview: CheckoutPreviewResponse;
  totalChanged: boolean;
  blockingWarning: boolean;
}
