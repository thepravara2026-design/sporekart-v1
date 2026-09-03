import { FC } from 'react';
import { Product } from '../types/catalog';
import { ProductCard } from './ProductCard';
import { Grid } from '../../../components/layout/Grid';

export interface ProductGridProps {
  products: Product[];
  onAddToCart?: (product: Product) => void;
  className?: string;
}

export const ProductGrid: FC<ProductGridProps> = ({ products, onAddToCart, className = '' }) => {
  return (
    <Grid minWidth="280px" gap="1.5rem" className={`product-grid ${className}`}>
      {products.map((product) => (
        <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} />
      ))}
    </Grid>
  );
};
