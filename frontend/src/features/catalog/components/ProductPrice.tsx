import { FC } from 'react';
import { formatPrice } from '../utils/catalogUtils';

export interface ProductPriceProps {
  price: number;
  currency?: string;
  className?: string;
  style?: React.CSSProperties;
}

export const ProductPrice: FC<ProductPriceProps> = ({
  price,
  currency = 'INR',
  className = '',
  style = {},
}) => {
  return (
    <span
      className={`product-price ${className}`}
      style={{
        fontSize: '1.25rem',
        fontWeight: 800,
        color: 'var(--accent-primary)',
        fontFamily: 'var(--font-family)',
        ...style,
      }}
    >
      {formatPrice(price, currency)}
    </span>
  );
};
