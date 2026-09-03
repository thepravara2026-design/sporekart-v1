import { FC, ChangeEvent } from 'react';
import { IconButton } from '../../../components/ui/IconButton';
import { Input } from '../../../components/ui/Input';
import { Plus, Minus } from 'lucide-react';
import { DEFAULT_MAX_PRODUCT_QUANTITY } from '../constants/catalogConstants';

export interface ProductQuantityProps {
  quantity: number;
  onQuantityChange: (newQuantity: number) => void;
  min?: number;
  max?: number;
  disabled?: boolean;
  className?: string;
}

/**
 * ProductQuantity — accessible quantity stepper.
 *
 * - Decrement / increment buttons and a numeric input.
 * - Minimum quantity defaults to 1, maximum defaults to the backend
 *   `cart.max-item-quantity` default of 50.
 * - Values are clamped to [min, max]; zero, negative, NaN and partial
 *   numeric input are normalized to valid quantities (UX only — the backend
 *   remains authoritative for inventory/quantity validation).
 * - All controls expose 40px touch targets and accessible names.
 */
export const ProductQuantity: FC<ProductQuantityProps> = ({
  quantity,
  onQuantityChange,
  min = 1,
  max = DEFAULT_MAX_PRODUCT_QUANTITY,
  disabled = false,
  className = '',
}) => {
  const clamped = (value: number) => {
    if (Number.isNaN(value)) return min;
    return Math.max(min, Math.min(max, Math.trunc(value)));
  };

  const handleDecrement = () => {
    if (quantity > min && !disabled) {
      onQuantityChange(quantity - 1);
    }
  };

  const handleIncrement = () => {
    if (quantity < max && !disabled) {
      onQuantityChange(quantity + 1);
    }
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const val = parseInt(e.target.value, 10);
    onQuantityChange(clamped(val));
  };

  return (
    <div
      className={`product-quantity-selector ${className}`}
      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
    >
      <IconButton
        icon={<Minus size={18} />}
        aria-label="Decrease quantity"
        onClick={handleDecrement}
        disabled={disabled || quantity <= min}
        variant="outline"
      />
      <Input
        type="number"
        inputMode="numeric"
        min={min}
        max={max}
        value={quantity}
        onChange={handleInputChange}
        disabled={disabled}
        aria-label="Quantity"
        style={{ width: '64px', height: '40px', textAlign: 'center', padding: '0.4rem 0.5rem' }}
      />
      <IconButton
        icon={<Plus size={18} />}
        aria-label="Increase quantity"
        onClick={handleIncrement}
        disabled={disabled || quantity >= max}
        variant="outline"
      />
    </div>
  );
};
