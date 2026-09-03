import { useMutation } from '@tanstack/react-query';
import {
  paymentApi,
  PaymentCheckoutDto,
  PaymentDto,
  PaymentVerificationCommand,
} from '../../../services/paymentApi';
import { ApiResponse } from '../../../types/api';
import { useToast } from '../../../components/ui/Toast';
import { getCheckoutErrorMessage } from '../utils/checkoutUtils';

/**
 * Initiate payment for a freshly placed order. Returns the provider order id
 * (and, for Razorpay, a key id) needed to render the hosted payment form.
 */
export const useInitiatePayment = () => {
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (orderId: string): Promise<ApiResponse<PaymentCheckoutDto>> => paymentApi.initiatePayment(orderId),
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Payment Could Not Be Started',
        message: getCheckoutErrorMessage(error),
      });
    },
  });
};

/** Verify the payment signature after the customer completes payment. */
export const useVerifyPayment = () => {
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (command: PaymentVerificationCommand): Promise<ApiResponse<PaymentDto>> =>
      paymentApi.verifyPayment(command),
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Payment Verification Failed',
        message: getCheckoutErrorMessage(error),
      });
    },
  });
};