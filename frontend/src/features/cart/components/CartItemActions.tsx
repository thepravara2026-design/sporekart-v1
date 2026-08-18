import { FC } from 'react';
import { Button } from '../../../components/ui/Button';
import { Trash2 } from 'lucide-react';

export interface CartItemActionsProps {
  productName: string;
  onRemove: () => void;
  isPending?: boolean;
  disabled?: boolean;
  className?: string;
}

/**
 * CartItemActions — remove action for a cart line. Disabled while the removal
 * is pending to prevent duplicate DELETE requests. The label is product
 * specific: "Remove {productName} from cart".
 */
export const CartItemActions: FC<CartItemActionsProps> = ({
  productName,
  onRemove,
  isPending = false,
  disabled = false,
  className = '',
}) => {
  return (
    <div className={`cart-item-actions ${className}`}>
      <Button
        type="button"
        variant="outline"
        size="sm"
        leftIcon={<Trash2 size={14} />}
        onClick={onRemove}
        disabled={disabled || isPending}
        isLoading={isPending}
        aria-label={`Remove ${productName} from cart`}
      >
        Remove
      </Button>
    </div>
  );
};