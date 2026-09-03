import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Drawer } from '../../../components/ui/Drawer';
import { useCart } from '../hooks/useCart';
import { formatPrice } from '../../catalog/utils/catalogUtils';
import { ShoppingCart } from 'lucide-react';

export interface CartDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  triggerRef?: React.RefObject<HTMLElement | null>;
}

/**
 * CartDrawer — slide-over quick-cart drawer (FD-11). Shows a compact view of
 * the cart lines and the subtotal straight from the shared cart cache, with a
 * shortcut into the full cart page. Price display only — totals stay
 * authoritative on the cart/checkout pages.
 */
export const CartDrawer: FC<CartDrawerProps> = ({ isOpen, onClose, triggerRef }) => {
  const { data: cartResponse, isLoading } = useCart();
  const cart = cartResponse?.data;
  const items = cart?.items ?? [];
  const currency = cart?.currency || 'INR';

  return (
    <Drawer isOpen={isOpen} onClose={onClose} title="Your Cart" triggerRef={triggerRef}>
      <div data-testid="cart-drawer" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {isLoading && <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Loading your cart...</p>}

        {!isLoading && items.length === 0 && (
          <div style={{ textAlign: 'center', padding: '1.5rem 0', color: 'var(--text-secondary)' }}>
            <ShoppingCart size={32} style={{ marginBottom: '0.5rem', opacity: 0.6 }} aria-hidden="true" />
            <p style={{ margin: 0, fontSize: '0.95rem' }}>Your cart is empty.</p>
          </div>
        )}

        {!isLoading && items.length > 0 && (
          <>
            <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {items.map((item) => (
                <li
                  key={item.id}
                  data-testid="cart-drawer-item"
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    gap: '0.75rem',
                    padding: '0.75rem',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'rgba(255,255,255,0.03)',
                    fontSize: '0.9rem',
                  }}
                >
                  <span style={{ minWidth: 0 }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)', display: 'block' }}>{item.productName}</span>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                      Qty {item.quantity}
                    </span>
                  </span>
                  <span style={{ fontWeight: 600, whiteSpace: 'nowrap', color: 'var(--text-primary)' }}>
                    {formatPrice(item.lineTotal, currency)}
                  </span>
                </li>
              ))}
            </ul>

            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                borderTop: '1px solid var(--border-color)',
                paddingTop: '0.75rem',
                fontSize: '1rem',
                fontWeight: 700,
              }}
            >
              <span>Subtotal</span>
              <span data-testid="cart-drawer-subtotal" style={{ color: 'var(--accent-primary)' }}>
                {formatPrice(cart?.subtotal ?? 0, currency)}
              </span>
            </div>

            <Link
              to="/cart"
              className="btn btn-primary"
              onClick={onClose}
              style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
            >
              View Cart
            </Link>
          </>
        )}
      </div>
    </Drawer>
  );
};