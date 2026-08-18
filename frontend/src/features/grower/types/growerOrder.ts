export type GrowerOrderStatusType =
  | 'PENDING_PAYMENT'
  | 'PAYMENT_CONFIRMED'
  | 'PROCESSING'
  | 'READY_FOR_FULFILMENT'
  | 'SHIPPED'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface GrowerOrderItem {
  id: string;
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface GrowerOrder {
  id: string;
  orderNumber: string;
  customerEmail: string;
  status: GrowerOrderStatusType;
  totalAmount: number;
  items: GrowerOrderItem[];
  createdAt: string;
  updatedAt: string;
  shippingAddress?: {
    street?: string;
    city?: string;
    state?: string;
    zipCode?: string;
  };
}

export interface OrderTransitionPayload {
  orderId: string;
  targetStatus: GrowerOrderStatusType;
  reason?: string;
  trackingNumber?: string;
}
