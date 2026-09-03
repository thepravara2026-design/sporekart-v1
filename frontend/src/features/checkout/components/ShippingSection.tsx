import { FC } from 'react';
import { Truck } from 'lucide-react';
import { CheckoutPreviewResponse } from '../../../services/cartApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';
import { useShippingOptions } from '../hooks/useShippingOptions';
import { Card, CardHeader, CardTitle, CardDescription } from '../../../components/ui/Card';

export interface ShippingSectionProps {
  preview?: CheckoutPreviewResponse;
  isPreviewLoading?: boolean;
}

/**
 * ShippingSection — displays the backend-computed delivery method and fee from
 * the checkout preview. The backend does not expose a shipping-options
 * selection endpoint, so exactly one authoritative option is shown and no
 * delivery dates or guarantees are fabricated.
 */
export const ShippingSection: FC<ShippingSectionProps> = ({ preview, isPreviewLoading = false }) => {
  const options = useShippingOptions(preview);

  if (isPreviewLoading && options.length === 0) {
    return (
      <Card data-testid="checkout-shipping-section">
        <CardHeader>
          <CardTitle>Delivery</CardTitle>
        </CardHeader>
        <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }} role="status">
          Calculating delivery options...
        </p>
      </Card>
    );
  }

  if (options.length === 0) {
    return (
      <Card data-testid="checkout-shipping-section">
        <CardHeader>
          <CardTitle>Delivery</CardTitle>
        </CardHeader>
        <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
          Delivery options are confirmed once the store has calculated your order.
        </p>
      </Card>
    );
  }

  const option = options[0];

  return (
    <Card data-testid="checkout-shipping-section">
      <CardHeader>
        <CardTitle>Delivery</CardTitle>
        <CardDescription>Calculated by the store from your order and address.</CardDescription>
      </CardHeader>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', fontSize: '0.95rem' }}>
        <Truck size={18} style={{ marginTop: '0.15rem', flexShrink: 0 }} aria-hidden="true" />
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 600 }}>{option.method}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{option.description}</div>
        </div>
        <div style={{ fontWeight: 700, whiteSpace: 'nowrap' }} data-testid="checkout-shipping-fee">
          {formatPrice(option.fee, option.currency)}
        </div>
      </div>
    </Card>
  );
};