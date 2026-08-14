import { FC } from 'react';
import { Product } from '../../../types/catalog';
import { ProductCard } from './ProductCard';

interface ProductGridProps {
  products: Product[];
}

export const ProductGrid: FC<ProductGridProps> = ({ products }) => {
  if (products.length === 0) {
    return null;
  }

  return (
    <div className="product-grid" data-testid="product-grid">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
};
