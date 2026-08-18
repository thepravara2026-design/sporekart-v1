import { OrderDto, OrderSummaryDto, OrderTimelineDto } from '../../../services/orderApi';
import { ShipmentDto, ShipmentTrackingResponseDto } from '../../../services/shippingApi';
import { ReturnEligibilityDto } from '../../../services/returnApi';
import { PageResponse } from '../../../types/api';

export const makeOrderSummary = (overrides: Partial<OrderSummaryDto> = {}): OrderSummaryDto => ({
  id: 'order-1',
  orderNumber: 'ORD-2026-000001',
  status: 'CONFIRMED',
  currency: 'INR',
  grandTotal: 567.82,
  itemCount: 2,
  createdAt: '2026-08-18T11:00:00Z',
  ...overrides,
});

export const makeOrder = (overrides: Partial<OrderDto> = {}): OrderDto => ({
  id: 'order-1',
  orderNumber: 'ORD-2026-000001',
  customerId: 'cust-1',
  status: 'CONFIRMED',
  currency: 'INR',
  subtotal: 498,
  discountTotal: 0,
  taxTotal: 44.82,
  shippingFee: 25,
  grandTotal: 567.82,
  shippingAddress: {
    fullName: 'A. Buyer',
    phone: '+919876543210',
    addressLine1: '42 Fungal Lane',
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560001',
    country: 'India',
  },
  items: [
    {
      id: 'oi-1',
      orderId: 'order-1',
      productId: 'prod-1',
      sku: 'SKU-OYSTER-01',
      productNameSnapshot: 'Blue Oyster Spawn',
      unitPrice: 249,
      quantity: 2,
      discountAmount: 0,
      taxAmount: 44.82,
      lineSubtotal: 498,
      lineTotal: 542.82,
      createdAt: '2026-08-18T11:00:00Z',
    },
  ],
  createdAt: '2026-08-18T11:00:00Z',
  updatedAt: '2026-08-18T11:00:00Z',
  ...overrides,
});

export const makeOrderPage = (content: OrderSummaryDto[], overrides: Partial<PageResponse<OrderSummaryDto>> = {}): PageResponse<OrderSummaryDto> => ({
  content,
  page: 0,
  size: 10,
  totalElements: content.length,
  totalPages: content.length > 0 ? 1 : 0,
  first: true,
  last: true,
  ...overrides,
});

export const makeOrderTimeline = (overrides: Partial<OrderTimelineDto> = {}): OrderTimelineDto => ({
  orderId: 'order-1',
  orderNumber: 'ORD-2026-000001',
  currentStatus: 'CONFIRMED',
  history: [
    {
      id: 'h-1',
      orderId: 'order-1',
      previousStatus: null,
      newStatus: 'CREATED',
      actorType: 'SYSTEM',
      actorId: null,
      createdAt: '2026-08-18T11:00:00Z',
    },
    {
      id: 'h-2',
      orderId: 'order-1',
      previousStatus: 'CREATED',
      newStatus: 'CONFIRMED',
      actorType: 'SYSTEM',
      actorId: null,
      createdAt: '2026-08-18T11:05:00Z',
    },
  ],
  ...overrides,
});

export const makeShipment = (overrides: Partial<ShipmentDto> = {}): ShipmentDto => ({
  id: 'ship-1',
  shipmentReference: 'SHP-2026-0001',
  orderId: 'order-1',
  orderReference: 'ORD-2026-000001',
  customerId: 'cust-1',
  status: 'IN_TRANSIT',
  provider: 'SHIPROCKET',
  awb: 'AWB-123456',
  trackingNumber: 'TRK-001',
  courierName: 'Delhivery',
  courierCode: 'DLV',
  estimatedDeliveryAt: '2026-08-20T00:00:00Z',
  trackingEvents: [],
  createdAt: '2026-08-19T08:00:00Z',
  updatedAt: '2026-08-19T08:00:00Z',
  ...overrides,
});

export const makeShipmentTracking = (overrides: Partial<ShipmentTrackingResponseDto> = {}): ShipmentTrackingResponseDto => ({
  shipmentReference: 'SHP-2026-0001',
  orderReference: 'ORD-2026-000001',
  status: 'IN_TRANSIT',
  awb: 'AWB-123456',
  courierName: 'Delhivery',
  estimatedDeliveryAt: '2026-08-20T00:00:00Z',
  timeline: [
    {
      id: 'ev-1',
      providerEventId: 'evt-provider-1',
      providerStatus: 'in_transit',
      normalizedStatus: 'IN_TRANSIT',
      description: 'Shipment picked up',
      location: 'Bengaluru',
      occurredAt: '2026-08-19T08:00:00Z',
    },
  ],
  ...overrides,
});

export const makeReturnEligibility = (overrides: Partial<ReturnEligibilityDto> = {}): ReturnEligibilityDto => ({
  orderId: 'order-1',
  orderReference: 'ORD-2026-000001',
  eligible: true,
  returnDeadline: '2026-09-03T10:00:00Z',
  items: [
    {
      orderItemId: 'oi-1',
      productId: 'prod-1',
      sku: 'SKU-OYSTER-01',
      productName: 'Blue Oyster Spawn',
      orderedQuantity: 2,
      previouslyReturnedQuantity: 0,
      returnableQuantity: 2,
      isReturnable: true,
    },
  ],
  ...overrides,
});