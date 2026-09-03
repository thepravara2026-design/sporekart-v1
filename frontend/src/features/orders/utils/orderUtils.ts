import { ApiError } from '../../../services/apiError';
import { CHECKOUT_ERROR_CODES } from '../../checkout/constants/checkoutConstants';
import { BadgeVariant } from '../../../components/ui/Badge';

/** Human-friendly, safe message for an order history / detail / action failure. */
export const getOrderErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    switch (error.code) {
      case CHECKOUT_ERROR_CODES.ORDER_NOT_FOUND:
        return 'Order not found. It may have been placed on another account.';
      case CHECKOUT_ERROR_CODES.ORDER_ACCESS_DENIED:
        return 'You do not have access to this order.';
      case CHECKOUT_ERROR_CODES.ORDER_NOT_CANCELLABLE:
        return 'This order can no longer be cancelled. It may already be in fulfilment.';
      case CHECKOUT_ERROR_CODES.PAYMENT_NOT_FOUND:
        return 'No payment record was found for this order.';
      default:
        break;
    }
    if (error.status === 401) {
      return 'Authentication required. Please sign in to view your orders.';
    }
    if (error.status === 403) {
      return 'You do not have permission to access this order.';
    }
    if (error.status === 404) {
      return 'Order not found. It may have been placed on another account.';
    }
    return error.message || 'Unable to load your orders. Please try again.';
  }
  if (error instanceof Error) {
    return 'Unable to load your orders. Please try again.';
  }
  return 'Unable to load your orders. Please try again.';
};

/** Human-friendly message for a shipment/tracking load failure. */
export const getShipmentErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    if (error.status === 404) {
      return 'No shipment is available for this order yet.';
    }
    return 'Unable to load shipment details. Please try again.';
  }
  return 'Unable to load shipment details. Please try again.';
};

/** Localized short date-time for display (e.g. order placed, event timestamps). */
export const formatOrderDate = (value?: string | null): string => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

/** Shipment statuses returned by the backend ShipmentStatus enum. */
export const ShipmentStatus = {
  CREATED: 'CREATED',
  READY_FOR_BOOKING: 'READY_FOR_BOOKING',
  BOOKING_PENDING: 'BOOKING_PENDING',
  BOOKED: 'BOOKED',
  PICKUP_SCHEDULED: 'PICKUP_SCHEDULED',
  PICKED_UP: 'PICKED_UP',
  IN_TRANSIT: 'IN_TRANSIT',
  OUT_FOR_DELIVERY: 'OUT_FOR_DELIVERY',
  DELIVERED: 'DELIVERED',
  DELIVERY_FAILED: 'DELIVERY_FAILED',
  CANCELLED: 'CANCELLED',
  RTO_INITIATED: 'RTO_INITIATED',
  RTO_IN_TRANSIT: 'RTO_IN_TRANSIT',
  RTO_DELIVERED: 'RTO_DELIVERED',
  EXCEPTION: 'EXCEPTION',
} as const;

export type ShipmentStatusValue = (typeof ShipmentStatus)[keyof typeof ShipmentStatus];

/** Human-readable label for a shipment status. */
export const getShipmentStatusLabel = (status: string): string => {
  switch (status) {
    case ShipmentStatus.CREATED:
    case ShipmentStatus.READY_FOR_BOOKING:
    case ShipmentStatus.BOOKING_PENDING:
      return 'Preparing Shipment';
    case ShipmentStatus.BOOKED:
      return 'Shipment Booked';
    case ShipmentStatus.PICKUP_SCHEDULED:
      return 'Pickup Scheduled';
    case ShipmentStatus.PICKED_UP:
      return 'Picked Up';
    case ShipmentStatus.IN_TRANSIT:
      return 'In Transit';
    case ShipmentStatus.OUT_FOR_DELIVERY:
      return 'Out for Delivery';
    case ShipmentStatus.DELIVERED:
      return 'Delivered';
    case ShipmentStatus.DELIVERY_FAILED:
      return 'Delivery Attempted';
    case ShipmentStatus.CANCELLED:
      return 'Shipment Cancelled';
    case ShipmentStatus.RTO_INITIATED:
      return 'Return to Origin Initiated';
    case ShipmentStatus.RTO_IN_TRANSIT:
      return 'Returning to Origin';
    case ShipmentStatus.RTO_DELIVERED:
      return 'Returned to Origin';
    case ShipmentStatus.EXCEPTION:
      return 'Exception';
    default:
      return status;
  }
};

/** Badge variant for a shipment status. */
export const getShipmentStatusVariant = (status: string): BadgeVariant => {
  switch (status) {
    case ShipmentStatus.DELIVERED:
      return 'success';
    case ShipmentStatus.CANCELLED:
    case ShipmentStatus.RTO_DELIVERED:
      return 'danger';
    case ShipmentStatus.DELIVERY_FAILED:
    case ShipmentStatus.EXCEPTION:
    case ShipmentStatus.RTO_INITIATED:
    case ShipmentStatus.RTO_IN_TRANSIT:
      return 'warning';
    case ShipmentStatus.IN_TRANSIT:
    case ShipmentStatus.OUT_FOR_DELIVERY:
    case ShipmentStatus.PICKED_UP:
    case ShipmentStatus.PICKUP_SCHEDULED:
      return 'info';
    default:
      return 'neutral';
  }
};
