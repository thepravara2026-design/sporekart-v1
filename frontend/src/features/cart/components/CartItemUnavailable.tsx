import { FC } from 'react';
import { Alert } from '../../../components/ui/Alert';

export interface CartItemUnavailableProps {
  productName: string;
  /** Reason surfaced by the backend (e.g. product no longer purchasable). */
  message?: string;
}

/**
 * CartItemUnavailable — explicit unavailable-item state.
 *
 * Rendered when the backend rejects a cart operation for this line (e.g.
 * `CATALOG_PRODUCT_NOT_PURCHASABLE`). Quantity changes are blocked and the
 * checkout path is prevented until the item is removed. The item is never
 * silently dropped — the remove action remains available.
 */
export const CartItemUnavailable: FC<CartItemUnavailableProps> = ({
  productName,
  message = 'This item is currently unavailable and must be removed before checkout.',
}) => {
  return (
    <div data-testid="cart-item-unavailable">
      <Alert variant="warning" title={`${productName} is unavailable`}>
        {message}
      </Alert>
    </div>
  );
};