import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Product } from '../types/catalog';
import { ProductPrice } from './ProductPrice';
import { ProductAvailability } from './ProductAvailability';
import { ProductActions } from './ProductActions';
import { ProductMetadata } from './ProductMetadata';

export interface ProductInfoProps {
  product: Product;
  className?: string;
}

export const ProductInfo: FC<ProductInfoProps> = ({ product, className = '' }) => {
  return (
    <div className={`product-info-wrapper ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
        <ProductAvailability status={product.status} />
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
        SKU: {product.sku}
      </div>

      <div style={{ marginTop: '0.5rem' }}>
        <ProductPrice price={product.price} currency={product.currency} style={{ fontSize: '2rem' }} />
      </div>

      <ProductActions product={product} />

      <ProductMetadata product={product} />
    </div>
  );
};
