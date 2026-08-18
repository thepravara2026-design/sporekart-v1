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
        icon={<ShoppingCart size={48} style={{ color: 'var(--accent-primary)' }} />}
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