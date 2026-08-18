import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../../../components/ui/Toast';
import { orderApi, AddressDto, OrderDto } from '../../../services/orderApi';
import { paymentApi, PaymentDto } from '../../../services/paymentApi';
import { getCheckoutErrorMessage } from '../utils/checkoutUtils';

export interface PlaceOrderInput {
  shippingAddress: AddressDto;
  customerNotes?: string;
}

export interface PlaceOrderResult {
  order: OrderDto;
  payment: PaymentDto;
}

/** Generates a client idempotency key (UUID) for order replay protection. */
const createIdempotencyKey = (): string =>
  typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : `ord-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

/**
 * PlaceOrder — orchestrates the checkout pipeline:
 *   create order → initiate payment → verify (mock provider completes instantly).
 *
 * The mock backend provider accepts any non-null signature except the literal
 * "INVALID_SIGNATURE", so the frontend simulates the provider redirect by
 * supplying stable mock provider values from the initiated attempt. On success
 * the cart cache is already invalidated by useCreateOrder.
 */
export const usePlaceOrder = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: async (input: PlaceOrderInput): Promise<PlaceOrderResult> => {
      const idempotencyKey = createIdempotencyKey();
      const orderResponse = await orderApi.createOrder({
        shippingAddress: input.shippingAddress,
        idempotencyKey,
        customerNotes: input.customerNotes,
      });
      const order = orderResponse.data;

      const paymentCheckout = await paymentApi.initiatePayment(order.id);
      const checkout = paymentCheckout.data;

      // Simulate the customer completing payment in the hosted provider flow.
      const paymentResponse = await paymentApi.verifyPayment({
        paymentReference: checkout.paymentReference,
        providerOrderId: checkout.providerOrderId,
        providerPaymentId: `pay_${checkout.providerOrderId}`,
        providerSignature: 'mock_provider_signature',
      });

      return { order, payment: paymentResponse.data };
    },
    onSuccess: (result) => {
      addToast({
        variant: 'success',
        title: 'Order Placed',
        message: `Order ${result.order.orderNumber} confirmed. A receipt is on its way.`,
      });
      navigate(`/checkout/confirmation?orderNumber=${result.order.orderNumber}`);
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Checkout Failed',
        message: getCheckoutErrorMessage(error),
      });
    },
  });
};