import { FC } from 'react';
import { Link } from 'react-router-dom';
import { CartItemDto } from '../../../services/cartApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';
import { Alert } from '../../../components/ui/Alert';
import { CartItemImage } from './CartItemImage';
import { CartItemPrice } from './CartItemPrice';
import { CartItemQuantity } from './CartItemQuantity';
import { CartItemActions } from './CartItemActions';
import { CartItemUnavailable } from './CartItemUnavailable';

export interface CartItemProps {
  item: CartItemDto;
  currency: string;
  /** True while this line's quantity mutation is in flight. */
  isUpdating?: boolean;
  /** True while this line's removal is in flight. */
  isRemoving?: boolean;
  /** True while any cart mutation is in flight (blocks all line controls). */
  blocked?: boolean;
  /** Inline error message for this line (server-confirmed failures). */
  error?: string | null;
  /** When the backend flags this line as unavailable, quantity is blocked. */
  unavailable?: boolean;
  unavailableMessage?: string;
  onQuantityChange: (quantity: number) => void;
  onRemove: () => void;
}

/**
 * CartItem — a single cart line. Displays the shared product thumbnail,
 * product name (linking to the product detail page), SKU, unit price, a
 * server-backed quantity stepper, line total and a remove action. Availability
 * and inline errors surface as alerts below the line.
 */
export const CartItem: FC<CartItemProps> = ({
  item,
  currency = 'INR',
  isUpdating = false,
  isRemoving = false,
  blocked = false,
  error = null,
  unavailable = false,
  unavailableMessage,
  onQuantityChange,
  onRemove,
}) => {
  const controlsDisabled = blocked || unavailable || isRemoving;

  return (
    <div
      className="cart-item"
      data-testid="cart-item"
      style={{
        display: 'flex',
        gap: '1rem',
        padding: '1.25rem 1rem',
        borderBottom: '1px solid var(--border-subtle)',
        borderLeft: '3px solid transparent',
        flexWrap: 'wrap',
        transition: 'background-color var(--transition-fast), border-left-color var(--transition-fast)',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.backgroundColor = 'rgba(16, 185, 129, 0.05)';
        e.currentTarget.style.borderLeftColor = 'rgba(16, 185, 129, 0.4)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.backgroundColor = 'transparent';
        e.currentTarget.style.borderLeftColor = 'transparent';
      }}
    >
      <CartItemImage productName={item.productName} />

      <div style={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
        <Link
          to={`/products/${item.productId}`}
          className="nav-link"
          style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--text-primary)', lineHeight: 1.35 }}
        >
          {item.productName}
        </Link>

        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          SKU: {item.sku}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <CartItemPrice unitPrice={item.unitPrice} currency={currency} />
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>each</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <CartItemQuantity
            productName={item.productName}
            quantity={item.quantity}
            onQuantityChange={onQuantityChange}
            disabled={controlsDisabled}
            isPending={isUpdating}
          />
          <span
            className="cart-item-line-total"
            data-testid="cart-item-line-total"
            style={{ fontSize: '1rem', fontWeight: 800, color: 'var(--text-primary)' }}
          >
            Line total: {formatPrice(item.lineTotal, currency)}
          </span>
        </div>
      </div>

      <CartItemActions
        productName={item.productName}
        onRemove={onRemove}
        isPending={isRemoving}
        disabled={blocked}
      />

      {unavailable && (
        <div style={{ width: '100%' }}>
          <CartItemUnavailable productName={item.productName} message={unavailableMessage} />
        </div>
      )}

      {error && !unavailable && (
        <div style={{ width: '100%' }}>
          <Alert variant="error" title={`Unable to update ${item.productName}`}>
            {error}
          </Alert>
        </div>
      )}
    </div>
  );
};