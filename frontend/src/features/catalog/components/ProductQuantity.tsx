import { FC, ChangeEvent } from 'react';
import { IconButton } from '../../../components/ui/IconButton';
import { Input } from '../../../components/ui/Input';
import { Plus, Minus } from 'lucide-react';

export interface ProductQuantityProps {
  quantity: number;
  onQuantityChange: (newQuantity: number) => void;
  min?: number;
  max?: number;
  disabled?: boolean;
  className?: string;
}

export const ProductQuantity: FC<ProductQuantityProps> = ({
  quantity,
  onQuantityChange,
  min = 1,
  max = 99,
  disabled = false,
  className = '',
}) => {
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
    if (isNaN(val)) {
      onQuantityChange(min);
    } else {
      const clamped = Math.max(min, Math.min(max, val));
      onQuantityChange(clamped);
    }
  };

  return (
    <div
      className={`product-quantity-selector ${className}`}
      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
    >
      <IconButton
        icon={<Minus size={16} />}
        aria-label="Decrease quantity"
        onClick={handleDecrement}
        disabled={disabled || quantity <= min}
        size="sm"
        variant="outline"
      />
      <Input
        type="number"
        min={min}
        max={max}
        value={quantity}
        onChange={handleInputChange}
        disabled={disabled}
        aria-label="Quantity"
        style={{ width: '64px', textAlign: 'center', padding: '0.4rem 0.5rem' }}
      />
      <IconButton
        icon={<Plus size={16} />}
        aria-label="Increase quantity"
        onClick={handleIncrement}
        disabled={disabled || quantity >= max}
        size="sm"
        variant="outline"
      />
    </div>
  );
};
