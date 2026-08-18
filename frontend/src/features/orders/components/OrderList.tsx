import { FC } from 'react';
import { OrderSummaryDto } from '../../../services/orderApi';
import { OrderCard } from './OrderCard';
import { OrderEmptyState } from './OrderEmptyState';

export interface OrderListProps {
  orders: OrderSummaryDto[];
}

/** The order history list, or an empty state when there are no orders. */
export const OrderList: FC<OrderListProps> = ({ orders }) => {
  if (orders.length === 0) {
    return <OrderEmptyState />;
  }

  return (
    <div data-testid="order-list" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      {orders.map((order) => (
        <OrderCard key={order.id} order={order} />
      ))}
    </div>
  );
};