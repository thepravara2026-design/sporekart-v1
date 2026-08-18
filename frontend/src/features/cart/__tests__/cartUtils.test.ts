import { describe, it, expect, afterEach } from 'vitest';
import { ApiError } from '../../../services/apiError';
import {
  isAuthenticated,
  getCartErrorMessage,
  getCartUnitCount,
  CART_ERROR_CODES,
} from '../utils/cartUtils';
import { makeCartItem } from './fixtures';

describe('cartUtils (FD-11)', () => {
  afterEach(() => {
    localStorage.clear();
  });

  it('detects authentication from the centralized token source', () => {
    expect(isAuthenticated()).toBe(false);
    localStorage.setItem('accessToken', 'jwt-token');
    expect(isAuthenticated()).toBe(true);
    localStorage.removeItem('accessToken');
    localStorage.setItem('token', 'jwt-token');
    expect(isAuthenticated()).toBe(true);
  });

  it('maps backend error codes to safe user-facing messages', () => {
    const notPurchasable = new ApiError('Product status is OUT_OF_STOCK', CART_ERROR_CODES.PRODUCT_NOT_PURCHASABLE, 400);
    expect(getCartErrorMessage(notPurchasable)).toContain('no longer available');

    const invalidQty = new ApiError('Quantity out of range', CART_ERROR_CODES.INVALID_QUANTITY, 400);
    expect(getCartErrorMessage(invalidQty)).toContain('not allowed');

    const itemNotFound = new ApiError('Cart item not found', CART_ERROR_CODES.ITEM_NOT_FOUND, 404);
    expect(getCartErrorMessage(itemNotFound)).toContain('refreshed');

    const unauthorized = new ApiError('Authentication required', 'UNAUTHORIZED', 401);
    expect(getCartErrorMessage(unauthorized)).toContain('Authentication required');
  });

  it('falls back to a generic message without exposing stack traces', () => {
    const boom = new Error('stack trace details');
    expect(getCartErrorMessage(boom)).not.toContain('stack trace');
    expect(getCartErrorMessage(null)).toBe('Unable to complete this cart operation. Please try again.');
  });

  it('computes the display unit count from line quantities', () => {
    const items = [makeCartItem(), makeCartItem({ id: 'item-2', quantity: 3 })];
    expect(getCartUnitCount(items)).toBe(5);
  });
});