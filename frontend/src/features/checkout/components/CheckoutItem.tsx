import { FC } from 'react';
import { CheckoutLineResponse } from '../../../services/cartApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';

export interface CheckoutItemProps {
  line: CheckoutLineResponse;
  currency: string;
}

/**
 * CheckoutItem — a single server-calculated line in the order review. Prices
 * and totals come from the backend checkout preview; a price-changed badge is
 * shown when the authoritative price differs from the cart snapshot.
 */
export const CheckoutItem: FC<CheckoutItemProps> = ({ line, currency }) => {
  return (
    <li
      data-testid="checkout-item"
      style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', fontSize: '0.9rem' }}
    >
      <span style={{ minWidth: 0 }}>
        <span style={{ fontWeight: 600, color: 'var(--text-primary)', display: 'block' }}>{line.productName}</span>
        <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
          {line.sku} · Qty {line.quantity} × {formatPrice(line.authoritativeUnitPrice, currency)}
        </span>
        {line.priceChanged && (
          <>
            <span
              style={{ display: 'inline-block', marginTop: '0.25rem', fontSize: '0.75rem', fontWeight: 700, color: '#f59e0b' }}
              role="status"
            >
              Price updated since you added it
            </span>
            <span style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              Was {formatPrice(line.cartUnitPrice, currency)}
            </span>
          </>
        )}
      </span>
      <span style={{ fontWeight: 600, whiteSpace: 'nowrap' }}>{formatPrice(line.lineTotal, currency)}</span>
    </li>
  );
};