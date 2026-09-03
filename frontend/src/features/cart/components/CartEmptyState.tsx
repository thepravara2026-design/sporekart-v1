import { FC } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart } from 'lucide-react';
import { EmptyState } from '../../../components/ui/EmptyState';
import { Button } from '../../../components/ui/Button';

/**
 * CartEmptyState — shown when the backend confirms an empty cart.
 */
export const CartEmptyState: FC = () => {
  return (
    <div data-testid="cart-empty">
      <EmptyState
        icon={
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '6rem',
              height: '6rem',
              borderRadius: 'var(--radius-full)',
              background: 'radial-gradient(circle, rgba(16, 185, 129, 0.18), rgba(16, 185, 129, 0.04))',
              border: '1px solid rgba(16, 185, 129, 0.3)',
            }}
          >
            <ShoppingCart size={40} style={{ color: 'var(--accent-primary)' }} />
          </div>
        }
        title="Your cart is empty"
        description="Browse the catalog to find premium mushroom spawn, cultures, substrate, and cultivation supplies."
        action={
          <Link to="/products">
            <Button variant="primary" size="lg">
              Continue Shopping
            </Button>
          </Link>
        }
      />
    </div>
  );
};