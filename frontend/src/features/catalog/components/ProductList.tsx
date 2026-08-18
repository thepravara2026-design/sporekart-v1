import { FC } from 'react';
import { Product } from '../types/catalog';
import { ProductCard } from './ProductCard';
import { Stack } from '../../../components/layout/Stack';

export interface ProductListProps {
  products: Product[];
  onAddToCart?: (product: Product) => void;
  className?: string;
}

export const ProductList: FC<ProductListProps> = ({ products, onAddToCart, className = '' }) => {
  return (
    <Stack gap={4} className={`product-list ${className}`}>
      {products.map((product) => (
        <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} />
      ))}
    </Stack>
  );
};
