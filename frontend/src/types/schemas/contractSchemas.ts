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
  productName: z.string(),
  sku: z.string(),
  unitPrice: z.number(),
  quantity: z.number(),
  lineSubtotal: z.number(),
});

export const CartSchema = z.object({
  id: z.string(),
  customerId: z.string(),
  items: z.array(CartItemSchema),
  totalQuantity: z.number(),
  subtotal: z.number(),
  status: z.string(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export const CheckoutPreviewResponseSchema = z.object({
  cartId: z.string(),
  itemSubtotal: z.number(),
  discountAmount: z.number(),
  shippingFee: z.number(),
  taxAmount: z.number(),
  grandTotal: z.number(),
  currency: z.string(),
  itemsCount: z.number(),
});
