import { z } from 'zod';

export const ApiErrorDetailsSchema = z.object({
  code: z.string(),
  message: z.string(),
  timestamp: z.string(),
  path: z.string().optional(),
  requestId: z.string().optional(),
});

export const ApiErrorResponseSchema = z.object({
  success: z.literal(false),
  error: ApiErrorDetailsSchema,
});

export const createApiResponseSchema = <T extends z.ZodTypeAny>(dataSchema: T) =>
  z.object({
    success: z.boolean(),
    data: dataSchema,
  });

export const createPageResponseSchema = <T extends z.ZodTypeAny>(itemSchema: T) =>
  z.object({
    content: z.array(itemSchema),
    page: z.number(),
    size: z.number(),
    totalElements: z.number(),
    totalPages: z.number(),
    first: z.boolean(),
    last: z.boolean(),
  });

// Domain Schemas
export const CategorySchema = z.object({
  id: z.string(),
  name: z.string(),
  slug: z.string(),
  description: z.string().nullable().optional(),
  status: z.enum(['ACTIVE', 'INACTIVE']),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const ProductSchema = z.object({
  id: z.string(),
  sku: z.string(),
  name: z.string(),
  description: z.string().nullable().optional(),
  price: z.number(),
  currency: z.string(),
  status: z.enum(['DRAFT', 'ACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED', 'ARCHIVED']),
  category: CategorySchema.nullable().optional(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const AuthTokenResponseSchema = z.object({
  accessToken: z.string(),
  refreshToken: z.string(),
  tokenType: z.string(),
  expiresInMs: z.number(),
  userId: z.string(),
  email: z.string(),
  role: z.string(),
  sessionId: z.string(),
});

export const UserProfileSchema = z.object({
  id: z.string(),
  email: z.string(),
  firstName: z.string().optional(),
  lastName: z.string().optional(),
  role: z.string(),
  status: z.string(),
});

export const CartItemSchema = z.object({
  id: z.string(),
  productId: z.string(),
  variantId: z.string().nullable().optional(),
  sku: z.string(),
  productName: z.string(),
  variantName: z.string().nullable().optional(),
  unitPrice: z.number(),
  quantity: z.number(),
  lineTotal: z.number(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const CartSchema = z.object({
  id: z.string(),
  customerId: z.string(),
  status: z.string(),
  currency: z.string(),
  subtotal: z.number(),
  itemCount: z.number(),
  items: z.array(CartItemSchema),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const CheckoutLineResponseSchema = z.object({
  cartItemId: z.string(),
  productId: z.string(),
  sku: z.string(),
  productName: z.string(),
  quantity: z.number(),
  cartUnitPrice: z.number(),
  authoritativeUnitPrice: z.number(),
  priceChanged: z.boolean(),
  lineSubtotal: z.number(),
  discountAmount: z.number(),
  taxAmount: z.number(),
  lineTotal: z.number(),
});

export const PriceBreakdownResponseSchema = z.object({
  subtotal: z.number(),
  discountTotal: z.number(),
  taxTotal: z.number(),
  shippingFee: z.number(),
  grandTotal: z.number(),
  currency: z.string(),
});

export const CheckoutWarningResponseSchema = z.object({
  type: z.string(),
  productId: z.string(),
  message: z.string(),
});

export const CheckoutPreviewResponseSchema = z.object({
  previewId: z.string(),
  cartId: z.string(),
  customerId: z.string(),
  currency: z.string(),
  items: z.array(CheckoutLineResponseSchema),
  breakdown: PriceBreakdownResponseSchema,
  warnings: z.array(CheckoutWarningResponseSchema),
  generatedAt: z.string(),
});

export const AddressSchema = z.object({
  fullName: z.string(),
  phone: z.string(),
  addressLine1: z.string(),
  addressLine2: z.string().nullable().optional(),
  city: z.string(),
  state: z.string(),
  postalCode: z.string(),
  country: z.string().nullable().optional(),
});

export const OrderItemDtoSchema = z.object({
  id: z.string(),
  orderId: z.string(),
  productId: z.string(),
  variantId: z.string().nullable().optional(),
  sku: z.string(),
  productNameSnapshot: z.string(),
  variantNameSnapshot: z.string().nullable().optional(),
  unitPrice: z.number(),
  quantity: z.number(),
  discountAmount: z.number(),
  taxAmount: z.number(),
  lineSubtotal: z.number(),
  lineTotal: z.number(),
  createdAt: z.string(),
});

export const OrderDtoSchema = z.object({
  id: z.string(),
  orderNumber: z.string(),
  customerId: z.string(),
  status: z.string(),
  currency: z.string(),
  subtotal: z.number(),
  discountTotal: z.number(),
  taxTotal: z.number(),
  shippingFee: z.number(),
  grandTotal: z.number(),
  idempotencyKey: z.string().nullable().optional(),
  shippingAddress: AddressSchema.nullable().optional(),
  customerNotes: z.string().nullable().optional(),
  items: z.array(OrderItemDtoSchema),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const OrderSummaryDtoSchema = z.object({
  id: z.string(),
  orderNumber: z.string(),
  status: z.string(),
  currency: z.string(),
  grandTotal: z.number(),
  itemCount: z.number(),
  createdAt: z.string(),
});

export const OrderStatusHistoryDtoSchema = z.object({
  id: z.string(),
  orderId: z.string(),
  previousStatus: z.string().nullable().optional(),
  newStatus: z.string(),
  reason: z.string().nullable().optional(),
  actorType: z.string(),
  actorId: z.string().nullable().optional(),
  createdAt: z.string(),
});

export const OrderTimelineDtoSchema = z.object({
  orderId: z.string(),
  orderNumber: z.string(),
  currentStatus: z.string(),
  history: z.array(OrderStatusHistoryDtoSchema),
});

export const ShipmentTrackingEventDtoSchema = z.object({
  id: z.string().optional(),
  providerEventId: z.string().optional(),
  providerStatus: z.string(),
  normalizedStatus: z.string(),
  description: z.string(),
  location: z.string().optional(),
  occurredAt: z.string(),
});

export const ShipmentTrackingResponseDtoSchema = z.object({
  shipmentReference: z.string(),
  orderReference: z.string(),
  status: z.string(),
  awb: z.string().optional(),
  courierName: z.string().optional(),
  estimatedDeliveryAt: z.string().optional(),
  timeline: z.array(ShipmentTrackingEventDtoSchema),
});

export const ShipmentDtoSchema = z.object({
  id: z.string(),
  shipmentReference: z.string(),
  orderId: z.string(),
  orderReference: z.string(),
  customerId: z.string(),
  status: z.string(),
  provider: z.string(),
  providerShipmentId: z.string().optional(),
  awb: z.string().optional(),
  trackingNumber: z.string().optional(),
  courierName: z.string().optional(),
  courierCode: z.string().optional(),
  estimatedDeliveryAt: z.string().optional(),
  bookedAt: z.string().optional(),
  pickedUpAt: z.string().optional(),
  deliveredAt: z.string().optional(),
  trackingEvents: z.array(ShipmentTrackingEventDtoSchema),
  version: z.number().optional(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const ItemEligibilityDtoSchema = z.object({
  orderItemId: z.string(),
  productId: z.string(),
  sku: z.string(),
  productName: z.string(),
  orderedQuantity: z.number(),
  previouslyReturnedQuantity: z.number(),
  returnableQuantity: z.number(),
  isReturnable: z.boolean(),
  reasonCode: z.string().nullable().optional(),
});

export const ReturnEligibilityDtoSchema = z.object({
  orderId: z.string(),
  orderReference: z.string(),
  eligible: z.boolean(),
  ineligibilityReason: z.string().nullable().optional(),
  deliveryTimestamp: z.string().nullable().optional(),
  returnDeadline: z.string().nullable().optional(),
  items: z.array(ItemEligibilityDtoSchema),
});

export const PaymentCheckoutDtoSchema = z.object({
  paymentId: z.string(),
  paymentReference: z.string(),
  attemptId: z.string(),
  attemptReference: z.string(),
  orderId: z.string(),
  amount: z.number(),
  currency: z.string(),
  provider: z.string(),
  providerOrderId: z.string(),
  keyId: z.string(),
});

export const PaymentAttemptDtoSchema = z.object({
  id: z.string(),
  paymentId: z.string(),
  attemptReference: z.string(),
  provider: z.string(),
  providerOrderId: z.string().nullable().optional(),
  providerPaymentId: z.string().nullable().optional(),
  status: z.string(),
  amount: z.number(),
  currency: z.string(),
  failureCode: z.string().nullable().optional(),
  failureReason: z.string().nullable().optional(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const PaymentDtoSchema = z.object({
  id: z.string(),
  paymentReference: z.string(),
  orderId: z.string(),
  customerId: z.string(),
  amount: z.number(),
  currency: z.string(),
  status: z.string(),
  provider: z.string(),
  activeAttemptId: z.string().nullable().optional(),
  attempts: z.array(PaymentAttemptDtoSchema),
  createdAt: z.string(),
  updatedAt: z.string(),
});
