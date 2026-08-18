import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartApi, CartDto } from '../../../services/cartApi';
import { useToast } from '../../../components/ui/Toast';
import { ApiResponse } from '../../../types/api';
import { getCartErrorMessage } from '../utils/cartUtils';
import { syncCartCache } from './useCartCache';

/**
 * Remove-item mutation. The cart is only removed from the cache after the
 * backend confirms; pending state on the trigger prevents duplicate removals.
 */
export const useRemoveCartItem = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (itemId: string): Promise<ApiResponse<CartDto>> => cartApi.removeItem(itemId),
    onSuccess: (response) => {
      syncCartCache(queryClient, response);
      addToast({
        variant: 'success',
        title: 'Item Removed',
        message: 'The item has been removed from your cart.',
      });
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Remove Failed',
        message: getCartErrorMessage(error),
      });
    },
  });
};
