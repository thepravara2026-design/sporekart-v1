import { useMemo } from 'react';
import { CheckoutPreviewResponse } from '../../../services/cartApi';
import { ShippingOption } from '../types/checkout';

/**
 * Shipping options available for the active checkout. The backend computes the
 * single shipping method during checkout preview (flat rate / free above
 * threshold) and does not expose a selection endpoint, so the frontend derives
 * exactly one option from the authoritative preview breakdown and never
 * invents methods, fees, or delivery dates.
 */
export const useShippingOptions = (preview?: CheckoutPreviewResponse): ShippingOption[] => {
  return useMemo(() => {
    if (!preview?.breakdown) return [];
    return [
      {
        id: 'standard',
        method: 'Standard Delivery',
        description: 'Shipping cost is calculated by the store during checkout.',
        fee: preview.breakdown.shippingFee,
        currency: preview.breakdown.currency,
      },
    ];
  }, [preview]);
};
