import { describe, it, expect } from 'vitest';
import {
  ApiErrorResponseSchema,
  ProductSchema,
  AuthTokenResponseSchema,
  CartSchema,
  CheckoutPreviewResponseSchema,
  createApiResponseSchema,
  createPageResponseSchema,
  OrderDtoSchema,
  OrderSummaryDtoSchema,
  OrderTimelineDtoSchema,
  OrderStatusHistoryDtoSchema,
  ShipmentDtoSchema,
  ShipmentTrackingResponseDtoSchema,
  ReturnEligibilityDtoSchema,
  ItemEligibilityDtoSchema,
} from '../../types/schemas/contractSchemas';

describe('Zod Contract Schemas Validation', () => {
  it('validates standard ApiErrorResponse structure', () => {
    const errorPayload = {
      success: false,
      error: {
        code: 'NOT_FOUND',
        message: 'Product not found',
        timestamp: '2026-08-18T11:00:00Z',
        path: '/api/v1/catalog/products/123',
        requestId: 'req-abc-789',
      },
    };

    const parsed = ApiErrorResponseSchema.safeParse(errorPayload);
    expect(parsed.success).toBe(true);
    if (parsed.success) {
      expect(parsed.data.error.code).toBe('NOT_FOUND');
      expect(parsed.data.error.requestId).toBe('req-abc-789');
    }
  });

  it('validates ProductSchema and PageResponse wrapper', () => {
    const productPayload = {
      id: 'p-101',
      sku: 'SKU-OYSTER-01',
      name: 'Blue Oyster Spawn',
      description: 'Premium quality grain spawn',
      price: 24.99,
      currency: 'INR',
      status: 'ACTIVE',
      category: {
        id: 'cat-1',
        name: 'Spawn',
        slug: 'spawn',
        status: 'ACTIVE',
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    };

    const parsedProduct = ProductSchema.safeParse(productPayload);
    expect(parsedProduct.success).toBe(true);

    const pagePayload = {
      content: [productPayload],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    };

    const pageSchema = createPageResponseSchema(ProductSchema);
    const parsedPage = pageSchema.safeParse(pagePayload);
    expect(parsedPage.success).toBe(true);

    const apiResponseSchema = createApiResponseSchema(pageSchema);
    const parsedApiResponse = apiResponseSchema.safeParse({ success: true, data: pagePayload });
    expect(parsedApiResponse.success).toBe(true);
  });

  it('validates AuthTokenResponseSchema structure', () => {
    const authPayload = {
      accessToken: 'jwt-access-token',
      refreshToken: 'jwt-refresh-token',
      tokenType: 'Bearer',
      expiresInMs: 3600000,
      userId: 'usr-1',
      email: 'buyer@sporekart.com',
      role: 'ROLE_BUYER',
      sessionId: 'sess-999',
    };

    const parsed = AuthTokenResponseSchema.safeParse(authPayload);
    expect(parsed.success).toBe(true);
  });

  it('validates CartSchema and CheckoutPreviewResponseSchema', () => {
    const cartPayload = {
      id: 'cart-1',
      customerId: 'cust-10',
      status: 'ACTIVE',
      currency: 'USD',
      subtotal: 30.0,
      itemCount: 2,
      items: [
        {
          id: 'item-1',
          productId: 'prod-10',
          variantId: null,
          sku: 'SKU-SHII-01',
          productName: 'Shiitake Substrate',
          variantName: null,
          unitPrice: 15.0,
          quantity: 2,
          lineTotal: 30.0,
          createdAt: '2026-08-18T00:00:00Z',
          updatedAt: '2026-08-18T00:00:00Z',
        },
      ],
      createdAt: '2026-08-18T00:00:00Z',
      updatedAt: '2026-08-18T00:00:00Z',
    };

    expect(CartSchema.safeParse(cartPayload).success).toBe(true);

    const previewPayload = {
      previewId: 'preview-1',
      cartId: 'cart-1',
      customerId: 'cust-10',
      currency: 'INR',
      items: [
        {
          cartItemId: 'item-1',
          productId: 'prod-10',
          sku: 'SKU-SHII-01',
          productName: 'Shiitake Substrate',
          quantity: 2,
          cartUnitPrice: 15.0,
          authoritativeUnitPrice: 15.0,
          priceChanged: false,
          lineSubtotal: 30.0,
          discountAmount: 0.0,
          taxAmount: 2.7,
          lineTotal: 32.7,
        },
      ],
      breakdown: {
        subtotal: 30.0,
        discountTotal: 0.0,
        taxTotal: 2.7,
        shippingFee: 5.0,
        grandTotal: 37.7,
        currency: 'INR',
      },
      warnings: [],
      generatedAt: '2026-08-18T11:00:00Z',
    };

    expect(CheckoutPreviewResponseSchema.safeParse(previewPayload).success).toBe(true);
  });

  it('validates order history page: OrderSummaryDto inside PageResponse wrapper', () => {
    const summaryPayload = {
      id: 'order-1',
      orderNumber: 'ORD-2026-000001',
      status: 'CONFIRMED',
      currency: 'INR',
      grandTotal: 567.82,
      itemCount: 2,
      createdAt: '2026-08-18T11:00:00Z',
    };

    expect(OrderSummaryDtoSchema.safeParse(summaryPayload).success).toBe(true);
    // The backend summary DTO has no customerId.
    const withCustomerId = { ...summaryPayload, customerId: 'cust-1' };
    expect(OrderSummaryDtoSchema.safeParse(withCustomerId).success).toBe(true);

    const pageSchema = createPageResponseSchema(OrderSummaryDtoSchema);
    const parsedPage = pageSchema.safeParse({
      content: [summaryPayload],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    });
    expect(parsedPage.success).toBe(true);

    const apiResponseSchema = createApiResponseSchema(pageSchema);
    expect(
      apiResponseSchema.safeParse({ success: true, data: parsedPage.data }).success
    ).toBe(true);
  });

  it('validates order detail and timeline DTOs', () => {
    const orderPayload = {
      id: 'order-1',
      orderNumber: 'ORD-2026-000001',
      customerId: 'cust-1',
      status: 'SHIPPED',
      currency: 'INR',
      subtotal: 498,
      discountTotal: 0,
      taxTotal: 44.82,
      shippingFee: 25,
      grandTotal: 567.82,
      idempotencyKey: null,
      shippingAddress: null,
      customerNotes: null,
      items: [],
      createdAt: '2026-08-18T11:00:00Z',
      updatedAt: '2026-08-18T11:00:00Z',
    };
    expect(OrderDtoSchema.safeParse(orderPayload).success).toBe(true);

    const historyPayload = {
      id: 'h-1',
      orderId: 'order-1',
      previousStatus: null,
      newStatus: 'SHIPPED',
      reason: null,
      actorType: 'SYSTEM',
      actorId: null,
      createdAt: '2026-08-18T11:00:00Z',
    };
    expect(OrderStatusHistoryDtoSchema.safeParse(historyPayload).success).toBe(true);

    const timelinePayload = {
      orderId: 'order-1',
      orderNumber: 'ORD-2026-000001',
      currentStatus: 'SHIPPED',
      history: [historyPayload],
    };
    expect(OrderTimelineDtoSchema.safeParse(timelinePayload).success).toBe(true);
  });

  it('validates shipment tracking response with timeline events (raw DTO, no envelope)', () => {
    const trackingPayload = {
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
    };
    expect(ShipmentTrackingResponseDtoSchema.safeParse(trackingPayload).success).toBe(true);

    const shipmentPayload = {
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
      trackingEvents: trackingPayload.timeline,
      version: 1,
      createdAt: '2026-08-19T08:00:00Z',
      updatedAt: '2026-08-19T08:00:00Z',
    };
    expect(ShipmentDtoSchema.safeParse(shipmentPayload).success).toBe(true);
  });

  it('validates return eligibility DTO (raw response, no envelope)', () => {
    const itemPayload = {
      orderItemId: 'oi-1',
      productId: 'prod-1',
      sku: 'SKU-OYSTER-01',
      productName: 'Blue Oyster Spawn',
      orderedQuantity: 2,
      previouslyReturnedQuantity: 0,
      returnableQuantity: 2,
      isReturnable: true,
      reasonCode: null,
    };
    expect(ItemEligibilityDtoSchema.safeParse(itemPayload).success).toBe(true);

    const eligibilityPayload = {
      orderId: 'order-1',
      orderReference: 'ORD-2026-000001',
      eligible: true,
      ineligibilityReason: null,
      deliveryTimestamp: '2026-08-20T10:00:00Z',
      returnDeadline: '2026-09-03T10:00:00Z',
      items: [itemPayload],
    };
    expect(ReturnEligibilityDtoSchema.safeParse(eligibilityPayload).success).toBe(true);
  });
});
