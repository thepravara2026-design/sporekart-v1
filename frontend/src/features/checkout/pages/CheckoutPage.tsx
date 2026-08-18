import { FC, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { CartValidationAlert } from '../../cart/components/CartValidationAlert';
import { CartEmptyState } from '../../cart/components/CartEmptyState';
import { CartSkeleton } from '../../cart/components/CartSkeleton';
import { CartErrorState } from '../../cart/components/CartErrorState';
import { CheckoutStepper } from '../components/CheckoutStepper';
import { CheckoutShippingForm } from '../components/CheckoutShippingForm';
import { CheckoutPaymentForm } from '../components/CheckoutPaymentForm';
import { useCart } from '../../cart/hooks/useCart';
import { useCheckoutPreview } from '../hooks/useCheckoutPreview';
import { usePlaceOrder } from '../hooks/usePlaceOrder';
import { isAuthenticated } from '../../cart/utils/cartUtils';
import { formatDestinationAddress } from '../utils/checkoutUtils';
import { CheckoutStepIndex } from '../constants/checkoutConstants';
import { AddressDto } from '../../../services/orderApi';
import { PaymentMethod } from '../constants/checkoutConstants';
import { ApiError } from '../../../services/apiError';

/**
 * CheckoutPage — authenticated multi-step checkout.
 *
 * Step flow: Shipping address → Payment (server-authoritative order summary +
 * payment method) → place order. The checkout preview is fetched from the
 * backend the moment the shipping address is known so displayed totals and any
 * price-change warnings are authoritative, never client-calculated.
 */
export const CheckoutPage: FC = () => {
  const { data: cartResponse, isLoading, isError, error, refetch } = useCart();
  const previewMutation = useCheckoutPreview();
  const placeOrder = usePlaceOrder();

  const [step, setStep] = useState<CheckoutStepIndex>(0);
  const [shippingAddress, setShippingAddress] = useState<AddressDto | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const requestedAddress = useRef<string | null>(null);

  const cart = cartResponse?.data;
  const items = cart?.items ?? [];
  const authenticated = isAuthenticated();
  const preview = previewMutation.data?.data;

  // Fetch the authoritative checkout preview once the address is submitted.
  useEffect(() => {
    if (!shippingAddress) return;
    const destinationAddress = formatDestinationAddress(shippingAddress);
    if (requestedAddress.current === destinationAddress) return;
    requestedAddress.current = destinationAddress;
    setPreviewError(null);
    previewMutation.mutate(
      { destinationAddress },
      {
        onError: (err: Error) => {
          setPreviewError(err instanceof ApiError ? err.message : 'We could not load your order summary.');
        },
      }
    );
  }, [shippingAddress, previewMutation]);

  const handleAddressSubmit = useCallback((address: AddressDto) => {
    setShippingAddress(address);
    setStep(1);
  }, []);

  const handlePlaceOrder = useCallback(
    (paymentMethod: PaymentMethod) => {
      void paymentMethod;
      if (!shippingAddress) return;
      setPreviewError(null);
      placeOrder.mutate({ shippingAddress });
    },
    [shippingAddress, placeOrder]
  );

  const breadcrumbs = useMemo(
    () => <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'Your Cart', path: '/cart' }, { label: 'Checkout' }]} />,
    []
  );

  return (
    <PageShell title="Checkout" breadcrumbs={breadcrumbs} className="checkout-page">
      {!authenticated && (
        <>
          <CartValidationAlert
            variant="warning"
            title="Sign in required"
            message="Sign in to complete checkout. Orders are tied to your authenticated account."
          />
          <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
            <Link to="/products" className="btn btn-secondary btn-sm">
              Continue Shopping
            </Link>
          </div>
        </>
      )}

      {authenticated && isLoading && <CartSkeleton />}

      {authenticated && isError && <CartErrorState error={error} onRetry={() => refetch()} />}

      {authenticated && !isLoading && !isError && cart && items.length === 0 && <CartEmptyState />}

      {authenticated && !isLoading && !isError && cart && items.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <CheckoutStepper currentStep={step} />

          {previewError && (
            <CartValidationAlert
              variant="warning"
              title="We could not complete your order"
              message={previewError}
            />
          )}

          {step === 0 && (
            <CheckoutShippingForm
              initialValues={shippingAddress ?? undefined}
              onSubmit={handleAddressSubmit}
            />
          )}

          {step === 1 && shippingAddress && (
            <CheckoutPaymentForm
              preview={preview}
              isPreviewLoading={previewMutation.isPending}
              isPlacingOrder={placeOrder.isPending}
              onSubmit={handlePlaceOrder}
              onBack={() => setStep(0)}
            />
          )}
        </div>
      )}
    </PageShell>
  );
};