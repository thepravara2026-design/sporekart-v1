import { FC, useState } from 'react';
import { Link } from 'react-router-dom';
import { Product, ProductVariant } from '../types/catalog';
import { ProductPrice } from './ProductPrice';
import { ProductAvailability } from './ProductAvailability';
import { ProductActions } from './ProductActions';
import { ProductMetadata } from './ProductMetadata';
import { useAddToCart } from '../hooks/useAddToCart';

export interface ProductInfoProps {
  product: Product;
  className?: string;
  quantity?: number;
  onQuantityChange?: (newQuantity: number) => void;
  selectedVariantId?: string | null;
  onSelectedVariantChange?: (variantId: string | null) => void;
  mutation?: ReturnType<typeof useAddToCart>;
}

export const ProductInfo: FC<ProductInfoProps> = ({
  product,
  className = '',
  quantity,
  onQuantityChange,
  selectedVariantId: controlledVariantId,
  onSelectedVariantChange,
  mutation,
}) => {
  const variants = product.variants || [];
  const [internalVariantId, setInternalVariantId] = useState<string | null>(
    variants.length > 0 ? variants[0].id : null
  );

  const selectedVariantId = controlledVariantId ?? internalVariantId;
  const setSelectedVariantId = onSelectedVariantChange ?? setInternalVariantId;

  const selectedVariant: ProductVariant | null =
    variants.find((v) => v.id === selectedVariantId) || (variants.length > 0 ? variants[0] : null);

  const currentPrice = selectedVariant ? selectedVariant.sellingPrice : product.price;
  const currentStrikeOutPrice = selectedVariant ? selectedVariant.strikeOutPrice : product.strikeOutPrice;
  const currentStatus = selectedVariant ? selectedVariant.status : product.status;
  const currentSku = selectedVariant ? selectedVariant.sku : product.sku;
  const currentStock = selectedVariant && typeof selectedVariant.availableQuantity === 'number'
    ? selectedVariant.availableQuantity
    : undefined;

  return (
    <div className={`product-info-wrapper ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
        <ProductAvailability status={currentStatus} availableStock={currentStock} />
        {product.category && (
          <Link
            to={`/products?categoryId=${product.category.id}`}
            style={{ fontSize: '0.85rem', color: 'var(--accent-primary)', fontWeight: 600, textDecoration: 'none' }}
          >
            {product.category.name}
          </Link>
        )}
      </div>

      <h1 style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-primary)', margin: 0, lineHeight: 1.2 }}>
        {product.name}
      </h1>

      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontFamily: 'var(--font-mono)' }}>
        SKU: {currentSku}
      </div>

      {variants.length > 1 && (
        <div style={{ marginTop: '0.5rem' }}>
          <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
            Select Size / Quantity:
          </label>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }} role="radiogroup" aria-label="Available variant sizes">
            {variants.map((v) => {
              const isSelected = selectedVariant?.id === v.id;
              return (
                <button
                  key={v.id}
                  type="button"
                  role="radio"
                  aria-checked={isSelected}
                  onClick={() => setSelectedVariantId(v.id)}
                  style={{
                    display: 'inline-flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    padding: '0.55rem 0.95rem',
                    borderRadius: 'var(--radius-md)',
                    border: isSelected ? '2px solid var(--accent-primary, #10b981)' : '1px solid rgba(255, 255, 255, 0.15)',
                    backgroundColor: isSelected ? 'rgba(16, 185, 129, 0.15)' : 'rgba(255, 255, 255, 0.05)',
                    color: isSelected ? 'var(--text-primary, #fff)' : 'var(--text-secondary, #9ca3af)',
                    cursor: 'pointer',
                    minWidth: '88px',
                    boxShadow: isSelected ? '0 0 12px 0 rgba(16, 185, 129, 0.2)' : 'none',
                    transition: 'all 0.15s ease-in-out',
                  }}
                >
                  <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{v.formattedQuantity}</span>
                  <span style={{ fontSize: '0.75rem', opacity: 0.85 }}>₹{v.sellingPrice}</span>
                </button>
              );
            })}
          </div>
        </div>
      )}

      <div style={{ marginTop: '0.5rem' }}>
        <ProductPrice price={currentPrice} strikeOutPrice={currentStrikeOutPrice} currency={product.currency} style={{ fontSize: '2rem' }} showDiscountBadge={true} />
      </div>

      <ProductActions
        product={product}
        selectedVariant={selectedVariant}
        quantity={quantity}
        onQuantityChange={onQuantityChange}
        mutation={mutation}
      />

      <ProductMetadata product={product} />
    </div>
  );
};
