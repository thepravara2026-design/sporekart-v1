import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Card } from '../../../components/ui/Card';
import { OrderStatusBadge } from './OrderStatusBadge';
import { OrderSummaryDto } from '../../../services/orderApi';
import { formatOrderDate } from '../utils/orderUtils';
import { formatPrice } from '../../catalog/utils/catalogUtils';

export interface OrderCardProps {
  order: OrderSummaryDto;
}

/** Summary card for one order in the customer history list. */
export const OrderCard: FC<OrderCardProps> = ({ order }) => {
  const currency = order.currency || 'INR';

  return (
    <Card data-testid="order-card" style={{ padding: '1.25rem', transition: 'transform var(--transition-normal), box-shadow var(--transition-normal), border-color var(--transition-normal)', boxShadow: 'var(--shadow-md)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
          <Link
            to={`/orders/${order.orderNumber}`}
            data-testid="order-card-link"
            style={{ fontWeight: 700, color: 'var(--accent-primary)', fontSize: '1rem', textDecoration: 'none' }}
          >
            {order.orderNumber}
          </Link>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            Placed {formatOrderDate(order.createdAt)}
          </span>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            {order.itemCount} {order.itemCount === 1 ? 'item' : 'items'}
          </span>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' }}>
          <OrderStatusBadge status={order.status} />
          <span style={{ fontWeight: 800, fontSize: '1.05rem', color: 'var(--text-primary)' }} data-testid="order-card-total">
            {formatPrice(order.grandTotal, currency)}
          </span>
        </div>
      </div>
    </Card>
  );
};
