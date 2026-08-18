import { FC, ChangeEvent } from 'react';
import { IconButton } from '../../../components/ui/IconButton';
import { Input } from '../../../components/ui/Input';
import { Plus, Minus } from 'lucide-react';
import { CART_MAX_ITEM_QUANTITY, CART_MIN_ITEM_QUANTITY } from '../constants/cartConstants';

export interface CartItemQuantityProps {
  /** Product name embedded into every control label for screen readers. */
  productName: string;
  quantity: number;
  onQuantityChange: (newQuantity: number) => void;
  min?: number;
  max?: number;
  /** Disabled while the server-backed update is pending or the line is blocked. */
  disabled?: boolean;
  /** True while this line's quantity mutation is in flight. */
  isPending?: boolean;
  className?: string;
}

/**
 * CartItemQuantity — accessible quantity stepper for a cart line.
 *
 * - Decrement / increment and a numeric input with product-specific labels
 *   (e.g. "Decrease quantity for White Oyster Mushroom Spawn").
 * - Minimum is 1 (backend `@Min(1)`); maximum mirrors the backend
 *   `cart.max-item-quantity` default of 50.
 * - The control is disabled while the server-backed update is pending, so
 *   duplicate requests cannot fire. The backend remains authoritative for
 *   quantity validation; the shown value only changes after confirmation.
 * - All controls expose 40px touch targets.
 */
export const CartItemQuantity: FC<CartItemQuantityProps> = ({
  productName,
  quantity,
  onQuantityChange,
  min = CART_MIN_ITEM_QUANTITY,
  max = CART_MAX_ITEM_QUANTITY,
  disabled = false,
  isPending = false,
  className = '',
}) => {
  const blocked = disabled || isPending;

  const clamped = (value: number) => {
    if (Number.isNaN(value)) return min;
    return Math.max(min, Math.min(max, Math.trunc(value)));
  };

  const handleDecrement = () => {
    if (quantity > min && !blocked) {
      onQuantityChange(quantity - 1);
    }
  };

  const handleIncrement = () => {
    if (quantity < max && !blocked) {
      onQuantityChange(quantity + 1);
    }
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const val = parseInt(e.target.value, 10);
    onQuantityChange(clamped(val));
  };

  return (
    <div
      className={`cart-item-quantity ${className}`}
      data-testid="cart-item-quantity"
      aria-busy={isPending || undefined}
      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
    >
      <IconButton
        icon={<Minus size={18} />}
        aria-label={`Decrease quantity for ${productName}`}
        onClick={handleDecrement}
        disabled={blocked || quantity <= min}
        variant="outline"
      />
      <Input
        type="number"
        inputMode="numeric"
        min={min}
        max={max}
        value={quantity}
        onChange={handleInputChange}
        disabled={blocked}
        aria-label={`Quantity for ${productName}`}
        style={{ width: '64px', height: '40px', textAlign: 'center', padding: '0.4rem 0.5rem' }}
      />
      <IconButton
        icon={<Plus size={18} />}
        aria-label={`Increase quantity for ${productName}`}
        onClick={handleIncrement}
        disabled={blocked || quantity >= max}
        variant="outline"
      />
      {isPending && (
        <span
          role="status"
          style={{ fontSize: '0.75rem', color: 'var(--text-muted)', minWidth: '3.5rem' }}
        >
          Updating...
        </span>
      )}
    </div>
  );
};