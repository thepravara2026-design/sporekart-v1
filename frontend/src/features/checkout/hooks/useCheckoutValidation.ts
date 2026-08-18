import { useCallback, useState } from 'react';
import { AddressDto } from '../../../services/orderApi';
import { CheckoutPreviewResponse } from '../../../services/cartApi';
import { useCheckoutPreview } from './useCheckoutPreview';
import { formatDestinationAddress, hasPreviewChanged, isBlockingCheckoutWarning } from '../utils/checkoutUtils';

export type CheckoutPreviewMutation = ReturnType<typeof useCheckoutPreview>;

export interface CheckoutRevalidationResult {
  preview: CheckoutPreviewResponse;
  totalChanged: boolean;
  blocking: boolean;
}

/**
 * Checkout revalidation — refresh the server-authoritative preview immediately
 * before the final order submission and detect whether totals or blocking
 * warnings changed since the customer reviewed them. Callers decide how to
 * surface the result (typically by blocking submission and prompting the
 * customer to review the updated preview).
 */
export const useCheckoutValidation = (previewMutation: CheckoutPreviewMutation) => {
  const [notice, setNotice] = useState<string | null>(null);

  const revalidate = useCallback(
    async (
      address: AddressDto,
      currentPreview?: CheckoutPreviewResponse | null
    ): Promise<CheckoutRevalidationResult> => {
      const response = await previewMutation.mutateAsync({
        destinationAddress: formatDestinationAddress(address),
      });
      const next = response.data;
      return {
        preview: next,
        totalChanged: hasPreviewChanged(currentPreview, next),
        blocking: next.warnings.some((warning) => isBlockingCheckoutWarning(warning.type)),
      };
    },
    [previewMutation]
  );

  return {
    revalidate,
    notice,
    setNotice,
    isPending: previewMutation.isPending,
  };
};
