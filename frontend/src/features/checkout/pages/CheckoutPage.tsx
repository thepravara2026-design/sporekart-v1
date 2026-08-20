import { FC, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { Alert } from '../../../components/ui/Alert';
import { CartValidationAlert } from '../../cart/components/CartValidationAlert';
import { CartEmptyState } from '../../cart/components/CartEmptyState';
import { useCart } from '../../cart/hooks/useCart';
import { useAuth } from '../../../context/AuthContext';
import { CheckoutStepper } from '../components/CheckoutStepper';
import { CheckoutPageHeader } from '../components/CheckoutPageHeader';
import { CustomerInformation } from '../components/CustomerInformation';
import { AddressSection } from '../components/AddressSection';
import { OrderReview } from '../components/OrderReview';
import { CheckoutPaymentForm } from '../components/CheckoutPaymentForm';
import { CheckoutValidationAlert } from '../components/CheckoutValidationAlert';
import { CheckoutSkeleton } from '../components/CheckoutSkeleton';
import { CheckoutErrorState } from '../components/CheckoutErrorState';
import { CheckoutSummary } from '../components/CheckoutSummary';
import { useCheckoutPreview } from '../hooks/useCheckoutPreview';
import { useCheckoutValidation } from '../hooks/useCheckoutValidation';
import { usePlaceOrder } from '../hooks/usePlaceOrder';
import { useCustomerProfile } from '../hooks/useCustomerProfile';
import { formatDestinationAddress } from '../utils/checkoutUtils';
import { CheckoutStepIndex } from '../constants/checkoutConstants';
import { AddressDto } from '../../../services/orderApi';
import { PaymentMethod } from '../constants/checkoutConstants';
import { ApiError } from '../../../services/apiError';

/**
 * CheckoutPage — authenticated multi-step checkout.
 *
 * Step flow: Customer & Delivery → Review & Confirm → Payment.
 *
 * - Customer information is prefilled from the backend /auth/me profile.
 * - The server-authoritative checkout preview is fetched the moment the
 *   delivery address is submitted and refreshed immediately before the final
 *   order submission; if totals or availability changed, submission is blocked
 *   until the customer reviews the updated preview.
 * - The final submission runs the backend order pipeline (create order →
 *   reserve inventory → initiate payment → verify payment); the backend
 *   remains authoritative for pricing, stock, and payment verification.
 */
export const CheckoutPage: FC = () => {
  const { data: cartResponse, isLoading, isError, error, refetch } = useCart();
  const { data: profile, isLoading: isProfileLoading, isError: isProfileError } = useCustomerProfile();
  const previewMutation = useCheckoutPreview();
  const { revalidate, notice, setNotice } = useCheckoutValidation(previewMutation);
  const placeOrder = usePlaceOrder();

  const [step, setStep] = useState<CheckoutStepIndex>(0);
  const [shippingAddress, setShippingAddress] = useState<AddressDto | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const requestedAddress = useRef<string | null>(null);

  const cart = cartResponse?.data;
  const items = cart?.items ?? [];
  const { isAuthenticated: authenticated } = useAuth();
  const preview = previewMutation.data?.data;

  const customerName = useMemo(() => {
    if (!profile) return undefined;
    const fullName = [profile.firstName, profile.lastName].filter(Boolean).join(' ');
    return fullName || profile.email;
  }, [profile]);

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
        onSuccess: () => setPreviewError(null),
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

  const handleReviewContinue = useCallback(() => {
    setStep(2);
  }, []);

  const handlePlaceOrder = useCallback(
    async (paymentMethod: PaymentMethod) => {
      if (!shippingAddress) return;
      if (placeOrder.isPending) return;

      setPreviewError(null);
      try {
        const result = await revalidate(shippingAddress, preview);
        if (result.totalChanged) {
          setNotice('The order total has changed. Please review your order.');
          return;
        }
        if (result.blocking) {
          setNotice('One or more items in your order are no longer available. Return to your cart to review the items.');
          return;
        }
        setNotice(null);
        placeOrder.mutate({ shippingAddress, paymentMethod });
      } catch (err) {
        setPreviewError(err instanceof ApiError ? err.message : 'We could not validate your order. Please try again.');
      }
    },
    [shippingAddress, preview, revalidate, setNotice, placeOrder]
  );

  const breadcrumbs = useMemo(
    () => <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'Your Cart', path: '/cart' }, { label: 'Checkout' }]} />,
    []
  );

  const prefillReady = !isProfileLoading && Boolean(profile);

  return (
    <PageShell title="Checkout" breadcrumbs={breadcrumbs} className="checkout-page">
      {authenticated && isLoading && <CheckoutSkeleton />}

      {authenticated && isError && <CheckoutErrorState error={error} onRetry={() => refetch()} />}

      {authenticated && !isLoading && !isError && cart && items.length === 0 && <CartEmptyState />}

      {authenticated && !isLoading && !isError && cart && items.length > 0 && (
        <>
          {isProfileError && (
            <Alert variant="warning" title="Profile unavailable">
              We could not load your profile. You can still proceed by entering your details manually.
            </Alert>
          )}
          <CheckoutPageHeader customerName={customerName} />
          <CheckoutStepper currentStep={step} />

          {previewError && (
            <div style={{ marginTop: '1.25rem' }}>
              <CartValidationAlert variant="warning" title="We could not complete your order" message={previewError} />
            </div>
          )}

          <div
            className="checkout-layout"
            style={{ marginTop: '1.25rem' }}
          >
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', minWidth: 0 }}>
              {step === 0 && (
                <>
                  <CustomerInformation profile={profile ?? null} isLoading={isProfileLoading} />
                  <AddressSection
                    key={shippingAddress ? 'submitted' : prefillReady ? 'prefill' : 'blank'}
                    initialValues={
                      shippingAddress ??
                      (profile ? { fullName: customerName ?? '' } as AddressDto : undefined)
                    }
                    submitLabel="Continue to Review"
                    onSubmit={handleAddressSubmit}
                  />
                </>
              )}

              {step === 1 && shippingAddress && (
                <>
                  <CheckoutValidationAlert warnings={preview?.warnings ?? []} />
                  <OrderReview
                    preview={preview}
                    address={shippingAddress}
                    isPreviewLoading={previewMutation.isPending}
                    onBack={() => setStep(0)}
                    onSubmit={handleReviewContinue}
                  />
                </>
              )}

              {step === 2 && shippingAddress && (
                <CheckoutPaymentForm
                  preview={preview}
                  isPreviewLoading={previewMutation.isPending}
                  isPlacingOrder={placeOrder.isPending}
                  revalidationNotice={notice}
                  onSubmit={handlePlaceOrder}
                  onBack={() => setStep(1)}
                />
              )}
            </div>

            {step === 0 && preview?.breakdown && (
              <div style={{ position: 'sticky', top: '1.5rem' }}>
                <Card data-testid="checkout-sticky-summary">
                  <CardHeader>
                    <CardTitle>Order Summary</CardTitle>
                  </CardHeader>
                  <CheckoutSummary
                    breakdown={preview.breakdown}
                    itemCount={preview.items.reduce((sum, item) => sum + item.quantity, 0)}
                  />
                </Card>
              </div>
            )}
          </div>
        </>
      )}
    </PageShell>
  );
};
