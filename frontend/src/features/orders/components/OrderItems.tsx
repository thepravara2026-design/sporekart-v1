import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { OrderDto } from '../../../services/orderApi';
import { OrderItem } from './OrderItem';

export interface OrderItemsProps {
  order: OrderDto;
}

/** Renders the authoritative item lines of an order. */
export const OrderItems: FC<OrderItemsProps> = ({ order }) => {
  const currency = order.currency || 'INR';
  const totalUnits = order.items.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <Card data-testid="order-items">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Items</h3>
        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          {totalUnits} {totalUnits === 1 ? 'item' : 'items'}
        </span>
      </div>
      <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
        {order.items.map((item) => (
          <OrderItem key={item.id} item={item} currency={currency} />
        ))}
      </ul>
    </Card>
  );
};