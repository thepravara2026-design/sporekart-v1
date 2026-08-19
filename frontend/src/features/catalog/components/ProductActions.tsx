import { FC, useState } from 'react';
import { Product, ProductVariant } from '../types/catalog';
import { ProductQuantity } from './ProductQuantity';
import { Button } from '../../../components/ui/Button';
import { useAddToCart } from '../hooks/useAddToCart';
import { isProductPurchasable, getStatusLabel } from '../utils/catalogUtils';
import { ShoppingCart } from 'lucide-react';

export interface ProductActionsProps {
  product: Product;
  selectedVariant?: ProductVariant | null;
  className?: string;
  /** Controlled quantity (lifted to the page so a mobile sticky action can share it). */
  quantity?: number;
  onQuantityChange?: (newQuantity: number) => void;
  /** Injected add-to-cart mutation (shared with the mobile sticky action). */
  mutation?: ReturnType<typeof useAddToCart>;
}

export const ProductActions: FC<ProductActionsProps> = ({
  product,
  selectedVariant,
  className = '',
  quantity: quantityProp,
  onQuantityChange: onQuantityChangeProp,
  mutation: mutationProp,
}) => {
  const [localQuantity, setLocalQuantity] = useState(1);
  const ownMutation = useAddToCart();
  const mutation = mutationProp ?? ownMutation;

  const quantity = quantityProp ?? localQuantity;
  const setQuantity = onQuantityChangeProp ?? setLocalQuantity;

  const currentStatus = selectedVariant ? selectedVariant.status : product.status;
  const purchasable = isProductPurchasable(currentStatus);
  const isPending = mutation.isPending;
  const disabled = !purchasable || isPending;

  const handleAddToCart = () => {
    if (disabled) return;
    const payload: { productId: string; quantity: number; variantId?: string } = {
      productId: product.id,
      quantity,
    };
    if (selectedVariant?.id) {
      payload.variantId = selectedVariant.id;
    }
    mutation.mutate(payload);
  };

  return (
    <div
      className={`product-actions ${className}`}
      style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '1.5rem' }}
    >
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '1.25rem' }}>
        <ProductQuantity
          quantity={quantity}
          onQuantityChange={setQuantity}
          disabled={disabled}
        />

        <Button
          variant={purchasable ? 'primary' : 'secondary'}
          size="lg"
          disabled={disabled}
          isLoading={isPending}
          leftIcon={purchasable ? <ShoppingCart size={20} /> : undefined}
          onClick={handleAddToCart}
          style={{ minWidth: '180px' }}
        >
          {purchasable ? 'Add to Cart' : getStatusLabel(currentStatus)}
        </Button>
      </div>

      {mutation.isError && (
        <p
          role="alert"
          data-testid="add-to-cart-error"
          style={{ fontSize: '0.85rem', color: 'var(--danger-color)', margin: 0 }}
        >
          {mutation.error instanceof Error
            ? mutation.error.message
            : 'Unable to add the item to your cart. Please try again.'}
        </p>
      )}
    </div>
  );
};
