import { BadgeVariant } from '../../../components/ui/Badge';

/** Order statuses returned by the backend OrderDto (mirrors OrderStatus enum). */
export const OrderStatus = {
  CREATED: 'CREATED',
  PAYMENT_PENDING: 'PAYMENT_PENDING',
  PAID: 'PAID',
  CONFIRMED: 'CONFIRMED',
  PROCESSING: 'PROCESSING',
  READY_FOR_FULFILMENT: 'READY_FOR_FULFILMENT',
  SHIPPED: 'SHIPPED',
  OUT_FOR_DELIVERY: 'OUT_FOR_DELIVERY',
  DELIVERED: 'DELIVERED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  PAYMENT_FAILED: 'PAYMENT_FAILED',
  EXPIRED: 'EXPIRED',
} as const;

export type OrderStatusValue = (typeof OrderStatus)[keyof typeof OrderStatus];

/** Human-readable label for an order status. */
export const getOrderStatusLabel = (status: string): string => {
  switch (status) {
    case OrderStatus.CREATED:
      return 'Order Received';
    case OrderStatus.PAYMENT_PENDING:
      return 'Payment Pending';
    case OrderStatus.PAID:
      return 'Paid';
    case OrderStatus.CONFIRMED:
      return 'Confirmed';
    case OrderStatus.PROCESSING:
      return 'Processing';
    case OrderStatus.READY_FOR_FULFILMENT:
      return 'Ready for Fulfilment';
    case OrderStatus.SHIPPED:
      return 'Shipped';
    case OrderStatus.OUT_FOR_DELIVERY:
      return 'Out for Delivery';
    case OrderStatus.DELIVERED:
      return 'Delivered';
    case OrderStatus.COMPLETED:
      return 'Completed';
    case OrderStatus.CANCELLED:
      return 'Cancelled';
    case OrderStatus.PAYMENT_FAILED:
      return 'Payment Failed';
    case OrderStatus.EXPIRED:
      return 'Expired';
    default:
      return status;
  }
};

/** Badge variant for an order status (visual semantics only — never logic). */
export const getOrderStatusVariant = (status: string): BadgeVariant => {
  switch (status) {
    case OrderStatus.CONFIRMED:
    case OrderStatus.PAID:
    case OrderStatus.DELIVERED:
    case OrderStatus.COMPLETED:
      return 'success';
    case OrderStatus.CREATED:
    case OrderStatus.PROCESSING:
    case OrderStatus.READY_FOR_FULFILMENT:
    case OrderStatus.SHIPPED:
    case OrderStatus.OUT_FOR_DELIVERY:
      return 'info';
    case OrderStatus.PAYMENT_PENDING:
    case OrderStatus.EXPIRED:
      return 'warning';
    case OrderStatus.CANCELLED:
    case OrderStatus.PAYMENT_FAILED:
      return 'danger';
    default:
      return 'neutral';
  }
};
