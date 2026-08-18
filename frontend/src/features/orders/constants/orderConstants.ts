/** Page size for the customer order history list (backend caps at 50). */
export const ORDER_PAGE_SIZE = 10;

/** Allowed order status filter values for the history list. */
export const ORDER_STATUS_FILTERS = [
  'ALL',
  'PAYMENT_PENDING',
  'PAID',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
] as const;

export type OrderStatusFilter = (typeof ORDER_STATUS_FILTERS)[number];

/**
 * Order statuses the backend OrderStatus.isCancellable() permits cancelling
 * from (CREATED, PAYMENT_PENDING, CONFIRMED, PAID, PROCESSING). Mirrored here
 * only to decide when to surface the Cancel action — the backend remains the
 * enforcement authority and rejects cancellations it does not allow.
 */
export const CANCELABLE_ORDER_STATUSES = [
  'CREATED',
  'PAYMENT_PENDING',
  'CONFIRMED',
  'PAID',
  'PROCESSING',
] as const;

