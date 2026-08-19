import { FC } from 'react';
import { formatPrice } from '../utils/catalogUtils';

export interface ProductPriceProps {
  price: number;
  strikeOutPrice?: number | null;
  currency?: string;
  className?: string;
  style?: React.CSSProperties;
  showDiscountBadge?: boolean;
}

export const ProductPrice: FC<ProductPriceProps> = ({
  price,
  strikeOutPrice,
  currency = 'INR',
  className = '',
  style = {},
  showDiscountBadge = false,
}) => {
  const hasValidStrikeOut = strikeOutPrice != null && strikeOutPrice > price;

  let discountPercent = 0;
  if (hasValidStrikeOut && strikeOutPrice > 0) {
    discountPercent = Math.round(((strikeOutPrice - price) / strikeOutPrice) * 100);
  }

  return (
    <div
      className={`product-price-container ${className}`}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.5rem',
        flexWrap: 'wrap',
        fontFamily: 'var(--font-family, inherit)',
        ...style,
      }}
    >
      {hasValidStrikeOut && (
        <span
          className="strike-out-price"
          style={{
            fontSize: '0.9em',
            fontWeight: 500,
            color: 'var(--text-muted, #94a3b8)',
            textDecoration: 'line-through',
          }}
          data-testid="strike-out-price"
        >
          {formatPrice(strikeOutPrice, currency)}
        </span>
      )}
      <span
        className="selling-price"
        style={{
          fontSize: '1.25rem',
          fontWeight: 800,
          color: 'var(--accent-primary, #059669)',
        }}
        data-testid="selling-price"
      >
        {formatPrice(price, currency)}
      </span>
      {hasValidStrikeOut && showDiscountBadge && discountPercent > 0 && (
        <span
          className="discount-badge"
          style={{
            fontSize: '0.75rem',
            fontWeight: 700,
            padding: '0.15rem 0.4rem',
            borderRadius: '0.25rem',
            backgroundColor: '#ef4444',
            color: '#ffffff',
            lineHeight: 1,
          }}
          data-testid="discount-badge"
        >
          {discountPercent}% OFF
        </span>
      )}
    </div>
  );
};
