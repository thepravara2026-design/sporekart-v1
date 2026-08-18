// Order domain types
export type {
  OrderDto,
  OrderItemDto,
  OrderSummaryDto,
  OrderStatusHistoryDto,
  OrderTimelineDto,
} from '../../services/orderApi';
export type {
  ShipmentDto,
  ShipmentTrackingResponseDto,
  TrackingEventDto,
} from '../../services/shippingApi';
export type {
  ReturnEligibilityDto,
  ItemEligibilityDto,
  ReturnDto,
} from '../../services/returnApi';

// Order constants & utilities
export * from './constants/orderConstants';
export * from './utils/orderStatus';
export {
  getOrderErrorMessage,
  getShipmentErrorMessage,
  formatOrderDate,
  getShipmentStatusLabel,
  getShipmentStatusVariant,
} from './utils/orderUtils';

// Order hooks
export * from './hooks/useOrder';
export * from './hooks/useShipment';
export * from './hooks/useReturnEligibility';

// Order components
export * from './components/OrderStatusBadge';
export * from './components/OrderCard';
export * from './components/OrderItem';
export * from './components/OrderItems';
export * from './components/OrderSummary';
export * from './components/OrderTimeline';
export * from './components/ShipmentStatus';
export * from './components/ShipmentTracking';
export * from './components/OrderActions';
export * from './components/OrderReturnHandoff';
export * from './components/OrderFilters';
export * from './components/OrderPagination';
export * from './components/OrderEmptyState';
export * from './components/OrderSkeleton';
export * from './components/OrderErrorState';
export * from './components/OrderList';

// Order pages
export * from './pages/OrdersPage';
export * from './pages/OrderDetailPage';