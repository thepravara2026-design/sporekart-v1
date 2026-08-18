import { FC } from 'react';
import { Product } from '../types/catalog';

export interface ProductMetadataProps {
  product: Product;
  className?: string;
}

export const ProductMetadata: FC<ProductMetadataProps> = ({ product, className = '' }) => {
  return (
    <div className={`product-metadata-wrapper ${className}`} style={{ marginTop: '1.5rem', borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '1.25rem' }}>
      <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
        Product Specifications
      </h4>
      <dl
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '1rem',
          margin: 0,
        }}
      >
        <div>
          <dt style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            SKU Identifier
          </dt>
          <dd style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)', margin: '0.2rem 0 0 0', fontFamily: 'var(--font-mono)' }}>
            {product.sku}
          </dd>
        </div>

        {product.category && (
          <div>
            <dt style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Category
            </dt>
            <dd style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)', margin: '0.2rem 0 0 0' }}>
              {product.category.name}
            </dd>
          </div>
        )}

        <div>
          <dt style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Availability Status
          </dt>
          <dd style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)', margin: '0.2rem 0 0 0' }}>
            {product.status}
          </dd>
        </div>

        <div>
          <dt style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Date Added
          </dt>
          <dd style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)', margin: '0.2rem 0 0 0' }}>
            {new Date(product.createdAt).toLocaleDateString()}
          </dd>
        </div>
      </dl>
    </div>
  );
};
