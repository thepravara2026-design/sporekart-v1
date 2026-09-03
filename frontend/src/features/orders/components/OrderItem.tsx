import { FC } from 'react';
import { OrderItemDto } from '../../../services/orderApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';

export interface OrderItemProps {
  item: OrderItemDto;
  currency: string;
}

/** One line item of an order (backend-snapshotted product name and prices). */
export const OrderItem: FC<OrderItemProps> = ({ item, currency }) => (
  <li
    data-testid="order-item"
    style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', fontSize: '0.9rem' }}
  >
    <span style={{ minWidth: 0 }}>
      <span style={{ fontWeight: 600, color: 'var(--text-primary)', display: 'block' }}>
        {item.productNameSnapshot}
      </span>
      <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
        SKU {item.sku} · Qty {item.quantity} × {formatPrice(item.unitPrice, currency)}
      </span>
    </span>
    <span style={{ fontWeight: 600, whiteSpace: 'nowrap' }} data-testid="order-item-total">
      {formatPrice(item.lineTotal, currency)}
    </span>
  </li>
);