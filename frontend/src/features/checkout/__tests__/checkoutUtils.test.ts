import { describe, it, expect } from 'vitest';
import { ApiError } from '../../../services/apiError';
import {
  getCheckoutErrorMessage,
  formatDestinationAddress,
  hasCheckoutWarnings,
  CHECKOUT_WARNING_TYPES,
} from '../utils/checkoutUtils';

describe('checkoutUtils (FD-11)', () => {
  it('maps backend checkout/order/payment error codes to safe messages', () => {
    expect(
      getCheckoutErrorMessage(new ApiError('Cart is empty', 'CHECKOUT_CART_EMPTY', 400))
    ).toContain('cart is empty');
    expect(
      getCheckoutErrorMessage(new ApiError('bad sig', 'PAYMENT_VERIFICATION_FAILED', 400))
    ).toContain('could not be verified');
    expect(
      getCheckoutErrorMessage(new ApiError('provider down', 'PAYMENT_PROVIDER_UNAVAILABLE', 503))
    ).toContain('temporarily unavailable');
    expect(getCheckoutErrorMessage(new ApiError('nope', 'ORDER_NOT_FOUND', 404))).toContain('Order not found');
    expect(getCheckoutErrorMessage(new ApiError('nope', 'ORDER_NOT_PAYABLE', 400))).toContain('cannot be paid');
    expect(getCheckoutErrorMessage(new ApiError('auth', 'AUTH', 401))).toContain('sign in');
    expect(getCheckoutErrorMessage(new ApiError('dup', 'DUPLICATE', 409))).toContain('already placed');
    expect(getCheckoutErrorMessage(new ApiError('Generic failure'))).toContain('Unable to complete checkout');
    expect(getCheckoutErrorMessage(new Error('Generic failure'))).toContain('Unable to complete checkout');
    expect(getCheckoutErrorMessage('unexpected')).toContain('Unable to complete checkout');
  });

  it('formats a destination address from the address fields', () => {
    expect(
      formatDestinationAddress({
        fullName: 'A. Buyer',
        phone: '+919876543210',
        addressLine1: '42 Fungal Lane',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560001',
        country: 'India',
      })
    ).toBe('42 Fungal Lane, Bengaluru, Karnataka, 560001');
  });

  it('omits optional/empty address parts', () => {
    expect(
      formatDestinationAddress({
        fullName: 'A. Buyer',
        phone: '+919876543210',
        addressLine1: '42 Fungal Lane',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560001',
      })
    ).toBe('42 Fungal Lane, Bengaluru, Karnataka, 560001');
  });

  it('detects presence of server checkout warnings', () => {
    expect(hasCheckoutWarnings([])).toBe(false);
    expect(
      hasCheckoutWarnings([{ type: CHECKOUT_WARNING_TYPES.PRICE_CHANGED, productId: 'p1', message: 'Price changed' }])
    ).toBe(true);
  });
});