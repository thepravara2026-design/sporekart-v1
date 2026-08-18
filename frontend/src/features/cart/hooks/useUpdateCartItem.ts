import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartApi, CartDto, UpdateCartItemCommand } from '../../../services/cartApi';
import { useToast } from '../../../components/ui/Toast';
import { ApiResponse } from '../../../types/api';
import { getCartErrorMessage } from '../utils/cartUtils';
import { syncCartCache } from './useCartCache';

export interface UpdateCartItemVariables {
  itemId: string;
  quantity: number;
}

/**
 * Server-backed quantity update mutation (non-optimistic).
 *
 * The UI only reflects the new quantity after the backend confirms, so the
 * displayed totals can never diverge from the authoritative cart. Per-item
 * inline errors are handled by callers via per-call onError; the hook owns
 * cache sync and failure toasts.
 */
export const useUpdateCartItem = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: ({ itemId, quantity }: UpdateCartItemVariables): Promise<ApiResponse<CartDto>> =>
      cartApi.updateItemQuantity(itemId, { quantity } satisfies UpdateCartItemCommand),
    onSuccess: (response) => {
      syncCartCache(queryClient, response);
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Quantity Update Failed',
        message: getCartErrorMessage(error),
      });
    },
  });
};
