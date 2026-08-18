import { FC } from 'react';
import { Link } from 'react-router-dom';
import { CartDto } from '../../../services/cartApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { ShoppingCart, ArrowRight } from 'lucide-react';

export interface CartSummaryProps {
  cart: CartDto;
  isClearing?: boolean;
  onClearCart: () => void;
  className?: string;
}

/**
 * CartSummary — order summary card. Displays ONLY values supplied by the
 * backend cart (item count and subtotal). Discount/tax/shipping rows are
 * intentionally omitted because the backend cart DTO does not expose them; the
 * frontend is not a pricing authority. Authoritative totals are shown on the
 * checkout page via the server-generated checkout preview.
 *
 * The checkout CTA navigates to the /checkout flow (FD-11).
 */
export const CartSummary: FC<CartSummaryProps> = ({ cart, isClearing = false, onClearCart, className = '' }) => {
  const currency = cart.currency || 'INR';

  return (
    <Card className={`cart-summary ${className}`} data-testid="cart-summary" style={{ position: 'sticky', top: '1.5rem' }}>
      <CardHeader>
        <CardTitle>Order Summary</CardTitle>
      </CardHeader>

      <dl style={{ margin: 0, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
          <dt style={{ color: 'var(--text-secondary)' }}>Items</dt>
          <dd style={{ margin: 0, fontWeight: 600, color: 'var(--text-primary)' }} data-testid="cart-summary-item-count">
            {cart.itemCount}
          </dd>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
          <dt style={{ color: 'var(--text-secondary)' }}>Subtotal</dt>
          <dd style={{ margin: 0, fontWeight: 600, color: 'var(--text-primary)' }} data-testid="cart-summary-subtotal">
            {formatPrice(cart.subtotal, currency)}
          </dd>
        </div>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            fontSize: '1.1rem',
            borderTop: '1px solid var(--border-color)',
            paddingTop: '0.75rem',
          }}
        >
          <dt style={{ fontWeight: 800, color: 'var(--text-primary)' }}>Total</dt>
          <dd style={{ margin: 0, fontWeight: 800, color: 'var(--accent-primary)' }} data-testid="cart-summary-total">
            {formatPrice(cart.subtotal, currency)}
          </dd>
        </div>
      </dl>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.5rem' }}>
        <Link
          to="/checkout"
          className="btn btn-primary btn-lg"
          aria-label="Proceed to Checkout"
          data-testid="checkout-cta"
          style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
        >
          <ShoppingCart size={18} />
          Proceed to Checkout
        </Link>
        <Link
          to="/products"
          className="btn btn-secondary"
          style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
        >
          <ArrowRight size={16} />
          Continue Shopping
        </Link>
        <button
          type="button"
          onClick={onClearCart}
          disabled={isClearing}
          aria-label="Clear all items from cart"
          data-testid="clear-cart-trigger"
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--danger-color)',
            cursor: isClearing ? 'not-allowed' : 'pointer',
            opacity: isClearing ? 0.55 : 1,
            fontSize: '0.85rem',
            fontWeight: 600,
            textDecoration: 'underline',
            padding: '0.4rem',
            alignSelf: 'center',
          }}
        >
          {isClearing ? 'Clearing cart...' : 'Clear Cart'}
        </button>
      </div>
    </Card>
  );
};