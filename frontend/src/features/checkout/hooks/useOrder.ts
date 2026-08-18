import { useMutation, useQueryClient } from '@tanstack/react-query';
import { orderApi, CreateOrderCommand, OrderDto } from '../../../services/orderApi';
import { ApiResponse } from '../../../types/api';
import { useToast } from '../../../components/ui/Toast';
import { getCheckoutErrorMessage } from '../utils/checkoutUtils';
import { CART_KEYS } from '../../cart/hooks/useCart';

// Order query keys and read hooks live in the orders feature (FD-13). Re-exported
// here so existing checkout surfaces keep their import paths.
export { ORDER_KEYS, useOrderByReference } from '../../orders/hooks/useOrder';

/**
 * Place an order from the server-confirmed cart. The backend transitions the
 * cart to CHECKED_OUT and reserves inventory. Callers drive the payment
 * initiation afterwards using the returned order id.
 */
export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (command: CreateOrderCommand): Promise<ApiResponse<OrderDto>> => orderApi.createOrder(command),
    onSuccess: () => {
      // Cart is now CHECKED_OUT — drop the cached active cart so the header
      // badge and cart page reconcile on next read.
      queryClient.removeQueries({ queryKey: CART_KEYS.all });
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Order Could Not Be Placed',
        message: getCheckoutErrorMessage(error),
      });
    },
  });
};
