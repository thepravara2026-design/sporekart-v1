import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartApi, CartDto } from '../../../services/cartApi';
import { useToast } from '../../../components/ui/Toast';
import { ApiResponse } from '../../../types/api';
import { getCartErrorMessage } from '../utils/cartUtils';
import { syncCartCache } from './useCartCache';

/**
 * Clear-cart mutation (backend `DELETE /api/v1/cart/items`). Only invoked from
 * a confirmed dialog; the cache is updated with the server's empty-cart
 * response on success.
 */
export const useClearCart = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (): Promise<ApiResponse<CartDto>> => cartApi.clearCart(),
    onSuccess: (response) => {
      syncCartCache(queryClient, response);
      addToast({
        variant: 'success',
        title: 'Cart Cleared',
        message: 'All items have been removed from your cart.',
      });
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Clear Cart Failed',
        message: getCartErrorMessage(error),
      });
    },
  });
};
