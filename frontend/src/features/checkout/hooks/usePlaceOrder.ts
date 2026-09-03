import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useRef } from 'react';
import { useToast } from '../../../components/ui/Toast';
import { orderApi, AddressDto, OrderDto } from '../../../services/orderApi';
import { paymentApi, PaymentDto } from '../../../services/paymentApi';
import { inventoryApi, ReservationDto } from '../../../services/inventoryApi';
import { getCheckoutErrorMessage } from '../utils/checkoutUtils';

export interface PlaceOrderInput {
  shippingAddress: AddressDto;
  customerNotes?: string;
}

export interface PlaceOrderResult {
  order: OrderDto;
  payment: PaymentDto;
  reservation: ReservationDto;
}

/** Generates a client idempotency key (UUID) for order replay protection. */
const createIdempotencyKey = (): string =>
  typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : `ord-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

/**
 * PlaceOrder — orchestrates the checkout pipeline required by the backend:
 *   create order → reserve inventory → initiate payment → verify payment.
 *
 * The backend requires an active inventory reservation before payment
 * initiation (payment initiation returns ORDER_NOT_PAYABLE otherwise). The
 * same idempotency key is reused for retries within a checkout session so a
 * retry after a mid-pipeline failure replays the existing order instead of
 * creating a duplicate. If payment fails after the reservation was created,
 * the reservation is released best-effort.
 *
 * The mock provider completes instantly, so the frontend simulates the hosted
 * provider return with stable mock provider values before calling the backend
 * verification endpoint. Verification itself is always backend-authoritative;
 * no secret is ever handled client-side.
 */
export const usePlaceOrder = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const idempotencyKeyRef = useRef<string | null>(null);

  return useMutation({
    mutationFn: async (input: PlaceOrderInput): Promise<PlaceOrderResult> => {
      idempotencyKeyRef.current ??= createIdempotencyKey();

      const orderResponse = await orderApi.createOrder({
        shippingAddress: input.shippingAddress,
        idempotencyKey: idempotencyKeyRef.current,
        customerNotes: input.customerNotes,
      });
      const order = orderResponse.data;

      const reservationResponse = await inventoryApi.reserveInventory(order.id);
      const reservation = reservationResponse.data;

      try {
        const paymentCheckout = await paymentApi.initiatePayment(order.id);
        const checkout = paymentCheckout.data;

        // Simulate the customer completing payment in the hosted provider flow.
        const paymentResponse = await paymentApi.verifyPayment({
          paymentReference: checkout.paymentReference,
          providerOrderId: checkout.providerOrderId,
          providerPaymentId: `pay_${checkout.providerOrderId}`,
          providerSignature: 'mock_provider_signature',
        });

        return { order, payment: paymentResponse.data, reservation };
      } catch (error) {
        // Payment did not complete — release the reserved stock so it can be
        // resold. Best-effort: release failures (e.g. already released) are ignored.
        await inventoryApi.releaseReservation(reservation.id, 'PAYMENT_FAILED').catch(() => {});
        throw error;
      }
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
