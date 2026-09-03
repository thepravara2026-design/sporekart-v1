import { describe, it, expect } from 'vitest';
import { ApiError } from '../../../services/apiError';
import {
  getCheckoutErrorMessage,
  formatDestinationAddress,
  formatAddressSummary,
  hasCheckoutWarnings,
  hasPreviewChanged,
  isBlockingCheckoutWarning,
  getCheckoutWarningVariant,
  getCheckoutWarningTitle,
  CHECKOUT_WARNING_TYPES,
} from '../utils/checkoutUtils';
import { CheckoutPreviewResponse } from '../../../services/cartApi';

const makePreview = (overrides: Partial<CheckoutPreviewResponse> = {}): CheckoutPreviewResponse => ({
  previewId: 'preview-1',
  cartId: 'cart-1',
  customerId: 'cust-1',
  currency: 'INR',
  items: [],
  breakdown: { subtotal: 498, discountTotal: 0, taxTotal: 44.82, shippingFee: 25, grandTotal: 567.82, currency: 'INR' },
  warnings: [],
  generatedAt: '2026-08-18T11:00:00Z',
  ...overrides,
});

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

  describe('warning classification (FD-12)', () => {
    it('treats only item-unavailable warnings as blocking', () => {
      expect(isBlockingCheckoutWarning(CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE)).toBe(true);
      expect(isBlockingCheckoutWarning(CHECKOUT_WARNING_TYPES.PRICE_CHANGED)).toBe(false);
      expect(isBlockingCheckoutWarning(CHECKOUT_WARNING_TYPES.STOCK_LIMITED)).toBe(false);
      expect(isBlockingCheckoutWarning(CHECKOUT_WARNING_TYPES.GENERAL)).toBe(false);
    });

    it('maps warning types to alert variants', () => {
      expect(getCheckoutWarningVariant(CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE)).toBe('error');
      expect(getCheckoutWarningVariant(CHECKOUT_WARNING_TYPES.PRICE_CHANGED)).toBe('warning');
      expect(getCheckoutWarningVariant(CHECKOUT_WARNING_TYPES.STOCK_LIMITED)).toBe('warning');
      expect(getCheckoutWarningVariant('UNKNOWN')).toBe('info');
    });

    it('maps warning types to human titles', () => {
      expect(getCheckoutWarningTitle(CHECKOUT_WARNING_TYPES.PRICE_CHANGED)).toBe('Prices have changed');
      expect(getCheckoutWarningTitle(CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE)).toBe('Item no longer available');
      expect(getCheckoutWarningTitle(CHECKOUT_WARNING_TYPES.STOCK_LIMITED)).toBe('Limited stock');
      expect(getCheckoutWarningTitle('UNKNOWN')).toBe('Please review');
    });
  });

  describe('preview change detection (FD-12)', () => {
    it('detects a grand-total change between previews', () => {
      const previous = makePreview();
      const next = makePreview({ breakdown: { ...previous.breakdown, grandTotal: 612.82 } });
      expect(hasPreviewChanged(previous, next)).toBe(true);
    });

    it('detects a currency change between previews', () => {
      const previous = makePreview();
      const next = makePreview({ breakdown: { ...previous.breakdown, currency: 'EUR' } });
      expect(hasPreviewChanged(previous, next)).toBe(true);
    });

    it('detects the appearance or disappearance of a blocking warning', () => {
      const previous = makePreview();
      const withBlocking = makePreview({
        warnings: [{ type: CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE, productId: 'prod-1', message: 'Sold out' }],
      });
      expect(hasPreviewChanged(previous, withBlocking)).toBe(true);
      expect(hasPreviewChanged(withBlocking, previous)).toBe(true);
    });

    it('ignores non-material changes such as advisory warnings', () => {
      const previous = makePreview();
      const withAdvisory = makePreview({
        warnings: [{ type: CHECKOUT_WARNING_TYPES.PRICE_CHANGED, productId: 'prod-1', message: 'Price moved' }],
      });
      expect(hasPreviewChanged(previous, withAdvisory)).toBe(false);
    });

    it('returns false when the previous preview is absent', () => {
      expect(hasPreviewChanged(null, makePreview())).toBe(false);
      expect(hasPreviewChanged(undefined, makePreview())).toBe(false);
    });
  });

  describe('formatAddressSummary (FD-12)', () => {
    it('joins the address fields for review surfaces', () => {
      expect(
        formatAddressSummary({
          fullName: 'A. Buyer',
          phone: '+919876543210',
          addressLine1: '42 Fungal Lane',
          addressLine2: '',
          city: 'Bengaluru',
          state: 'Karnataka',
          postalCode: '560001',
          country: 'India',
        })
      ).toBe('A. Buyer, 42 Fungal Lane, Bengaluru, Karnataka, 560001, India');
    });

    it('omits empty optional fields', () => {
      expect(
        formatAddressSummary({
          fullName: 'A. Buyer',
          phone: '+919876543210',
          addressLine1: '42 Fungal Lane',
          addressLine2: '',
          city: 'Bengaluru',
          state: 'Karnataka',
          postalCode: '560001',
          country: 'India',
        })
      ).toBe('A. Buyer, 42 Fungal Lane, Bengaluru, Karnataka, 560001, India');
    });
  });
});
