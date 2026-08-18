import { FC } from 'react';
import { formatPrice } from '../../catalog/utils/catalogUtils';

export interface CartItemPriceProps {
  unitPrice: number;
  currency: string;
  className?: string;
}

/**
 * CartItemPrice — formatted unit price for a cart line. Formatting only; the
 * backend remains the pricing authority for the amount itself.
 */
export const CartItemPrice: FC<CartItemPriceProps> = ({ unitPrice, currency, className = '' }) => {
  return (
    <span
      className={`cart-item-price ${className}`}
      data-testid="cart-item-price"
      style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-primary)' }}
    >
      {formatPrice(unitPrice, currency)}
    </span>
  );
};