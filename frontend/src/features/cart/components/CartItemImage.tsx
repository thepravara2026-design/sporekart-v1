import { FC } from 'react';
import { ProductImage } from '../../catalog/components/ProductImage';

export interface CartItemImageProps {
  /** Product name used as the accessible alt text. */
  productName: string;
  /** Backend-provided image URL (cart contract currently has none → fallback). */
  src?: string | null;
  size?: number;
}

/**
 * CartItemImage — fixed-dimension product thumbnail reusing the shared
 * `ProductImage` system (4:3 aspect, lazy loading, branded fallback). Fixed
 * dimensions prevent cumulative layout shift while the cart resolves.
 */
export const CartItemImage: FC<CartItemImageProps> = ({
  productName,
  src,
  size = 88,
}) => {
  return (
    <div
      className="cart-item-image"
      data-testid="cart-item-image"
      style={{ flexShrink: 0 }}
    >
      <ProductImage
        src={src}
        alt={productName}
        loading="lazy"
        style={{ width: `${size}px`, maxWidth: '100%' }}
      />
    </div>
  );
};