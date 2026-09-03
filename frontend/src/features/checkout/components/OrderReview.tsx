import { FC } from 'react';
import { Link } from 'react-router-dom';
import { CheckoutPreviewResponse } from '../../../services/cartApi';
import { AddressDto } from '../../../services/orderApi';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { ArrowRight } from 'lucide-react';
import { AddressCard } from './AddressCard';
import { ShippingSection } from './ShippingSection';
import { CheckoutItem } from './CheckoutItem';
import { CheckoutSummary } from './CheckoutSummary';

export interface OrderReviewProps {
  preview?: CheckoutPreviewResponse;
  address: AddressDto;
  isPreviewLoading?: boolean;
  isSubmitting?: boolean;
  onBack: () => void;
  onSubmit: () => void;
}

/**
 * OrderReview — the confirmation surface the customer reviews before payment.
 * Products, quantities, prices, the selected address, the delivery method and
 * every total are rendered from the backend preview; the frontend never
 * overrides backend totals.
 */
export const OrderReview: FC<OrderReviewProps> = ({
  preview,
  address,
  isPreviewLoading = false,
  isSubmitting = false,
  onBack,
  onSubmit,
}) => {
  const breakdown = preview?.breakdown;
  const currency = breakdown?.currency ?? 'INR';

  return (
    <div data-testid="checkout-order-review">
      <Card data-testid="checkout-review-items">
        <CardHeader>
          <CardTitle>Products</CardTitle>
        </CardHeader>
        {preview ? (
          <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            {preview.items.map((line) => (
              <CheckoutItem key={line.cartItemId} line={line} currency={currency} />
            ))}
          </ul>
        ) : (
          <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }} role="status">
            {isPreviewLoading ? 'Calculating your order totals...' : 'Order totals are calculated by the store.'}
          </p>
        )}
      </Card>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1.25rem',
          marginTop: '1.25rem',
        }}
      >
        <AddressCard address={address} />
        <ShippingSection preview={preview} isPreviewLoading={isPreviewLoading} />
      </div>

      <div style={{ marginTop: '1.25rem' }}>
        <Card>
          <CardHeader>
            <CardTitle>Order Summary</CardTitle>
          </CardHeader>
          {breakdown ? (
            <CheckoutSummary
              breakdown={breakdown}
              itemCount={preview?.items.reduce((sum, item) => sum + item.quantity, 0)}
            />
          ) : (
            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }} role="status">
              {isPreviewLoading ? 'Calculating your order totals...' : 'Order totals are calculated by the store.'}
            </p>
          )}
        </Card>
      </div>

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          gap: '0.75rem',
          marginTop: '1.25rem',
          flexWrap: 'wrap',
        }}
      >
        <Button type="button" variant="secondary" onClick={onBack} disabled={isSubmitting}>
          Back to Delivery
        </Button>
        <Link to="/cart" style={{ textDecoration: 'none' }}>
          <Button type="button" variant="ghost" disabled={isSubmitting}>
            Edit Cart
          </Button>
        </Link>
        <Button type="button" size="lg" isLoading={isSubmitting} rightIcon={<ArrowRight size={16} />} onClick={onSubmit}>
          Continue to Payment
        </Button>
      </div>
    </div>
  );
};