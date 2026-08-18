import { FC, useState } from 'react';
import { Product } from '../types/catalog';
import { ProductQuantity } from './ProductQuantity';
import { Button } from '../../../components/ui/Button';
import { useAddToCart } from '../hooks/useAddToCart';
import { ShoppingCart } from 'lucide-react';

export interface ProductActionsProps {
  product: Product;
  className?: string;
}

export const ProductActions: FC<ProductActionsProps> = ({ product, className = '' }) => {
  const [quantity, setQuantity] = useState(1);
  const addToCartMutation = useAddToCart();

  const isOutOfStock = product.status === 'OUT_OF_STOCK' || product.status === 'DISCONTINUED';

  const handleAddToCart = () => {
    if (isOutOfStock) return;
    addToCartMutation.mutate({
      productId: product.id,
      quantity,
    });
  };

  return (
    <div
      className={`product-actions ${className}`}
      style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '1.25rem', marginTop: '1.5rem' }}
    >
      <ProductQuantity
        quantity={quantity}
        onQuantityChange={setQuantity}
        disabled={isOutOfStock || addToCartMutation.isPending}
      />

      <Button
        variant={isOutOfStock ? 'secondary' : 'primary'}
        size="lg"
        disabled={isOutOfStock || addToCartMutation.isPending}
        isLoading={addToCartMutation.isPending}
        leftIcon={<ShoppingCart size={20} />}
        onClick={handleAddToCart}
        style={{ minWidth: '180px' }}
      >
        {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
      </Button>
    </div>
  );
};
