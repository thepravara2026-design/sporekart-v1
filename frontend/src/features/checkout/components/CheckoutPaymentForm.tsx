import { FC } from 'react';
import { CheckoutPreviewResponse } from '../../../services/cartApi';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { CheckoutSummary } from './CheckoutSummary';
import { CheckoutSubmit } from './CheckoutSubmit';
import { CheckoutValidationAlert } from './CheckoutValidationAlert';
import { PaymentMethod } from '../constants/checkoutConstants';

export interface CheckoutPaymentFormProps {
  preview?: CheckoutPreviewResponse;
  isPreviewLoading?: boolean;
  isPlacingOrder?: boolean;
  revalidationNotice?: string | null;
  onSubmit: (paymentMethod: PaymentMethod) => void;
  onBack: () => void;
}

/**
 * CheckoutPaymentForm — payment step. Composes the payment-method selection
 * and Place Order CTA (CheckoutSubmit) with the server-authoritative order
 * summary (CheckoutSummary) and any checkout / revalidation warnings.
 */
export const CheckoutPaymentForm: FC<CheckoutPaymentFormProps> = ({
  preview,
  isPreviewLoading = false,
  isPlacingOrder = false,
  revalidationNotice = null,
  onSubmit,
  onBack,
}) => {
  const breakdown = preview?.breakdown;
  const isComputing = isPreviewLoading && !breakdown;

  return (
    <div data-testid="checkout-payment-form">
      <CheckoutValidationAlert warnings={preview?.warnings ?? []} revalidationNotice={revalidationNotice} />

      <div
        className="checkout-layout"
        style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', alignItems: 'start' }}
      >
        <CheckoutSubmit isPlacingOrder={isPlacingOrder} onSubmit={onSubmit} onBack={onBack} />

        {breakdown ? (
          <Card data-testid="checkout-order-summary">
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <CheckoutSummary
              breakdown={breakdown}
              itemCount={preview?.items.reduce((sum, item) => sum + item.quantity, 0)}
            />
          </Card>
        ) : (
          <Card data-testid="checkout-order-summary">
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }} role="status">
              {isComputing ? 'Calculating your order totals...' : 'Order totals are calculated by the store.'}
            </p>
          </Card>
        )}
      </div>
    </div>
  );
};
