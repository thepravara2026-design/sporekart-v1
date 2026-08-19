import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { GrowerProduct } from '../types/growerProduct';
import { GrowerStatusBadge } from './GrowerStatusBadge';
import { ProductImageCarousel } from '../../catalog/components/ProductImageCarousel';
import { formatCurrency, getProductStatusMeta } from '../utils/growerUtils';

export interface GrowerProductCardProps {
  product: GrowerProduct;
}

export const GrowerProductCard: FC<GrowerProductCardProps> = ({ product }) => {
  const statusMeta = getProductStatusMeta(product.status);
  const carouselImages = [
    ...(product.images || []),
    ...(product.imageUrl ? [product.imageUrl] : []),
  ].filter(Boolean);

  return (
    <Card
      style={{
        padding: '1.25rem',
        backgroundColor: '#0d231a',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: '0.5rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem',
      }}
    >
      {carouselImages.length > 0 && (
        <ProductImageCarousel images={carouselImages} alt={product.name} loading="lazy" />
      )}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 600, display: 'block', marginBottom: '0.25rem' }}>
            {product.sku}
          </span>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, color: '#f3f4f6', margin: 0 }}>
            {product.name}
          </h3>
        </div>
        <GrowerStatusBadge label={statusMeta.label} variant={statusMeta.variant} />
      </div>

      <p style={{ fontSize: '0.875rem', color: '#9ca3af', margin: 0, minHeight: '2.5rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
        {product.description || 'Organic cultivated mycology spawn item.'}
      </p>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto', paddingTop: '0.5rem', borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
        <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>
          Category: {product.categoryName || 'General'}
        </span>
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
          {product.strikeOutPrice != null && product.strikeOutPrice > product.price && (
            <span style={{ fontSize: '0.85rem', textDecoration: 'line-through', color: '#6b7280', fontWeight: 500 }} data-testid="grower-strike-out-price">
              {formatCurrency(product.strikeOutPrice, product.currency)}
            </span>
          )}
          <span style={{ fontSize: '1.125rem', fontWeight: 700, color: '#10b981' }}>
            {formatCurrency(product.price, product.currency)}
          </span>
        </span>
      </div>
    </Card>
  );
};
