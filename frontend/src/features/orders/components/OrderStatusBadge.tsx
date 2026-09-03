import { FC } from 'react';
import { Badge } from '../../../components/ui/Badge';
import { getOrderStatusLabel, getOrderStatusVariant } from '../utils/orderStatus';

export interface OrderStatusBadgeProps {
  status: string;
  className?: string;
}

/** Pill displaying an order status with visual semantics (backend label only). */
export const OrderStatusBadge: FC<OrderStatusBadgeProps> = ({ status, className = '' }) => (
  <Badge variant={getOrderStatusVariant(status)} className={className}>
    {getOrderStatusLabel(status)}
  </Badge>
);
