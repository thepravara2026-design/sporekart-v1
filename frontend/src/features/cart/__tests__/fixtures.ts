import { CartDto, CartItemDto } from '../../../services/cartApi';
import { ApiResponse } from '../../../types/api';

export const makeCartItem = (overrides: Partial<CartItemDto> = {}): CartItemDto => ({
  id: 'item-1',
  productId: 'prod-1',
  variantId: null,
  sku: 'SKU-OYSTER-01',
  productName: 'Blue Oyster Mushroom Spawn',
  variantName: null,
  unitPrice: 249.0,
  quantity: 2,
  lineTotal: 498.0,
  createdAt: '2026-08-18T00:00:00Z',
  updatedAt: '2026-08-18T00:00:00Z',
  ...overrides,
});

export const makeCart = (overrides: Partial<CartDto> = {}): CartDto => ({
  id: 'cart-1',
  customerId: 'cust-1',
  status: 'ACTIVE',
  currency: 'INR',
  subtotal: 498.0,
  itemCount: 2,
  items: [makeCartItem()],
  createdAt: '2026-08-18T00:00:00Z',
  updatedAt: '2026-08-18T00:00:00Z',
  ...overrides,
});

export const makeCartResponse = (overrides: Partial<CartDto> = {}): ApiResponse<CartDto> => ({
  success: true,
  data: makeCart(overrides),
});