import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartApi, AddCartItemCommand } from '../../../services/cartApi';
import { useToast } from '../../../components/ui/Toast';

export const useAddToCart = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (command: AddCartItemCommand) => cartApi.addItem(command),
    onSuccess: (_response, variables) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      addToast({
        variant: 'success',
        title: 'Added to Cart',
        message: `${variables.quantity} item(s) successfully added to your cart.`,
      });
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Add to Cart Failed',
        message: error.message || 'Unable to add item to cart. Please try again.',
      });
    },
  });
};
