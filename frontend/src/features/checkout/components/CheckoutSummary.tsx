import { FC } from 'react';
import { PriceBreakdownResponse } from '../../../services/cartApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';

export interface CheckoutSummaryProps {
  breakdown: PriceBreakdownResponse;
  itemCount?: number;
}

/**
 * CheckoutSummary — renders only the pricing values supplied by the backend
 * checkout preview. No client-side pricing formula is used; discount, tax and
 * shipping are displayed only when the backend reports a non-zero amount.
 */
export const CheckoutSummary: FC<CheckoutSummaryProps> = ({ breakdown, itemCount }) => {
  const currency = breakdown.currency || 'INR';

  return (
    <dl
      data-testid="checkout-summary"
      style={{ margin: 0, display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.9rem' }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <dt style={{ color: 'var(--text-secondary)' }}>Items {typeof itemCount === 'number' ? `(${itemCount})` : ''}</dt>
        <dd style={{ margin: 0, fontWeight: 600 }} data-testid="checkout-summary-subtotal">
          {formatPrice(breakdown.subtotal, currency)}
        </dd>
      </div>
      {breakdown.discountTotal > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <dt style={{ color: 'var(--text-secondary)' }}>Discount</dt>
          <dd style={{ margin: 0, fontWeight: 600, color: '#10b981' }} data-testid="checkout-summary-discount">
            −{formatPrice(breakdown.discountTotal, currency)}
          </dd>
        </div>
      )}
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <dt style={{ color: 'var(--text-secondary)' }}>Tax</dt>
        <dd style={{ margin: 0, fontWeight: 600 }} data-testid="checkout-summary-tax">
          {formatPrice(breakdown.taxTotal, currency)}
        </dd>
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <dt style={{ color: 'var(--text-secondary)' }}>Shipping</dt>
        <dd style={{ margin: 0, fontWeight: 600 }} data-testid="checkout-summary-shipping">
          {formatPrice(breakdown.shippingFee, currency)}
        </dd>
      </div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          fontSize: '1.05rem',
          borderTop: '1px solid var(--border-color)',
          paddingTop: '0.75rem',
          marginTop: '0.25rem',
        }}
      >
        <dt style={{ fontWeight: 800 }}>Total</dt>
        <dd style={{ margin: 0, fontWeight: 800, color: 'var(--accent-primary)' }} data-testid="checkout-summary-total">
          {formatPrice(breakdown.grandTotal, currency)}
        </dd>
      </div>
    </dl>
  );
};
