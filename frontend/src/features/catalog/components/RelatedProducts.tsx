import { FC } from 'react';
import { useRelatedProducts } from '../hooks/useRelatedProducts';
import { ProductGrid } from './ProductGrid';
import { ProductCardSkeleton } from './ProductCardSkeleton';
import { Grid } from '../../../components/layout/Grid';

export interface RelatedProductsProps {
  categoryId?: string;
  currentProductId?: string;
  limit?: number;
  className?: string;
}

export const RelatedProducts: FC<RelatedProductsProps> = ({
  categoryId,
  currentProductId,
  limit = 4,
  className = '',
}) => {
  const { relatedProducts, isLoading, isError } = useRelatedProducts({
    categoryId,
    currentProductId,
    limit,
  });

  if (!categoryId || (isError && relatedProducts.length === 0)) {
    return null;
  }

  if (isLoading) {
    return (
      <div className={`related-products-section ${className}`} style={{ marginTop: '3rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '1rem' }}>
          Related Mushroom Cultures & Spawn
        </h3>
        <Grid minWidth="260px" gap={6}>
          {Array.from({ length: limit }).map((_, idx) => (
            <ProductCardSkeleton key={idx} />
          ))}
        </Grid>
      </div>
    );
  }

  if (relatedProducts.length === 0) {
    return null;
  }

  return (
    <div className={`related-products-section ${className}`} style={{ marginTop: '3rem' }}>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '1rem' }}>
        Related Mushroom Cultures & Spawn
      </h3>
      <ProductGrid products={relatedProducts} />
    </div>
  );
};
