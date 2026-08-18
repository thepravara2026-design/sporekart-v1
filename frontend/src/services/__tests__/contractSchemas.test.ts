import { describe, it, expect } from 'vitest';
import {
  ApiErrorResponseSchema,
  ProductSchema,
  AuthTokenResponseSchema,
  CartSchema,
  CheckoutPreviewResponseSchema,
  createApiResponseSchema,
  createPageResponseSchema,
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
});
