import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartApi, CheckoutPreviewRequest, CheckoutPreviewResponse } from '../../../services/cartApi';
import { ApiResponse } from '../../../types/api';
import { useToast } from '../../../components/ui/Toast';
import { getCheckoutErrorMessage } from '../utils/checkoutUtils';

export const CHECKOUT_KEYS = {
  all: ['checkout'] as const,
  preview: ['checkout', 'preview'] as const,
} as const;

/**
 * Server-authoritative checkout preview. The backend calculates pricing and
 * emits warnings (price changes, unavailable items, stock limits); the
 * frontend never computes totals itself.
 */
export const useCheckoutPreview = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (request: CheckoutPreviewRequest): Promise<ApiResponse<CheckoutPreviewResponse>> =>
      cartApi.generateCheckoutPreview(request),
    onSuccess: (response) => {
      queryClient.setQueryData<ApiResponse<CheckoutPreviewResponse>>(CHECKOUT_KEYS.preview, response);
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Checkout Preview Failed',
        message: getCheckoutErrorMessage(error),
      });
    },
  });
};