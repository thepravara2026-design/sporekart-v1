import { FC } from 'react';
import { Link } from 'react-router-dom';
import { EmptyState } from '../../../components/ui/EmptyState';
import { PackageOpen } from 'lucide-react';

/** Friendly empty state for a customer with no orders yet. */
export const OrderEmptyState: FC = () => (
  <EmptyState
    icon={<PackageOpen size={48} style={{ color: 'var(--accent-primary)' }} />}
    title="No orders yet"
    description="When you place an order, it will appear here with live status, tracking, and cancellation options."
    action={
      <Link to="/products" className="btn btn-primary btn-sm">
        Start Shopping
      </Link>
    }
  />
);