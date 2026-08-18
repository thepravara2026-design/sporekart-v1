import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartApi, AddCartItemCommand, CartDto } from '../../../services/cartApi';
import { useToast } from '../../../components/ui/Toast';
import { ApiError } from '../../../services/apiError';
import { ApiResponse } from '../../../types/api';

const CART_QUERY_KEY = ['cart'] as const;

/**
 * Add-to-cart mutation integrated with the shared cart API and TanStack Query.
 *
 * - On success the cached cart query is refreshed so cart counts/totals stay in
 *   sync without a full page reload. Product page state is preserved.
 * - Success is only announced after the backend confirms the mutation.
 * - Failures surface a meaningful toast; auth/inventory rejections retain the
 *   error message for inline display alongside preserved user selections.
 */
export const useAddToCart = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (command: AddCartItemCommand) => cartApi.addItem(command),
    onSuccess: (response, variables) => {
      queryClient.setQueryData<ApiResponse<CartDto>>(CART_QUERY_KEY, response);
      queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY });
      addToast({
        variant: 'success',
        title: 'Added to Cart',
        message: `${variables.quantity} item(s) successfully added to your cart.`,
      });
    },
    onError: (error: Error) => {
      const message =
        error instanceof ApiError
          ? error.message || 'Unable to add the item to your cart. Please try again.'
          : 'Unable to add the item to your cart. Please try again.';
      addToast({
        variant: 'error',
        title: 'Add to Cart Failed',
        message,
      });
    },
  });
};
