import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { OrderReview } from '../OrderReview';
import { CheckoutPreviewResponse } from '../../../../services/cartApi';
import { AddressDto } from '../../../../services/orderApi';

const makePreview = (overrides: Partial<CheckoutPreviewResponse> = {}): CheckoutPreviewResponse => ({
  previewId: 'preview-1',
  cartId: 'cart-1',
  customerId: 'cust-1',
  currency: 'INR',
  items: [
    {
      cartItemId: 'item-1',
      productId: 'prod-1',
      sku: 'SKU-OYSTER-01',
      productName: 'Blue Oyster Spawn',
      quantity: 2,
      cartUnitPrice: 249,
      authoritativeUnitPrice: 249,
      priceChanged: false,
      lineSubtotal: 498,
      discountAmount: 0,
      taxAmount: 44.82,
      lineTotal: 542.82,
    },
  ],
  breakdown: { subtotal: 498, discountTotal: 0, taxTotal: 44.82, shippingFee: 25, grandTotal: 567.82, currency: 'INR' },
  warnings: [],
  generatedAt: '2026-08-18T11:00:00Z',
  ...overrides,
});

const makeAddress = (): AddressDto => ({
  fullName: 'A. Buyer',
  phone: '+919876543210',
  addressLine1: '42 Fungal Lane',
  addressLine2: '',
  city: 'Bengaluru',
  state: 'Karnataka',
  postalCode: '560001',
  country: 'India',
});

const renderReview = (props: Partial<React.ComponentProps<typeof OrderReview>> = {}) =>
  render(
    <MemoryRouter>
      <OrderReview preview={makePreview()} address={makeAddress()} onBack={vi.fn()} onSubmit={vi.fn()} {...props} />
    </MemoryRouter>
  );

describe('OrderReview (FD-12)', () => {
  it('renders products, address, delivery method, and backend totals', () => {
    renderReview();
    expect(screen.getByTestId('checkout-order-review')).toBeInTheDocument();
    expect(screen.getByTestId('checkout-item')).toBeInTheDocument();
    expect(screen.getByTestId('checkout-address-card')).toBeInTheDocument();
    expect(screen.getByText('42 Fungal Lane')).toBeInTheDocument();
    expect(screen.getByTestId('checkout-shipping-section')).toBeInTheDocument();
    expect(screen.getByTestId('checkout-shipping-fee')).toHaveTextContent('₹25.00');
    expect(screen.getByTestId('checkout-summary-total')).toHaveTextContent('₹567.82');
    expect(screen.getByRole('button', { name: 'Continue to Payment' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Back to Delivery' })).toBeInTheDocument();
  });

  it('shows a computing status while the preview is pending', () => {
    renderReview({ preview: undefined, isPreviewLoading: true });
    expect(screen.getAllByText('Calculating your order totals...').length).toBeGreaterThan(0);
  });

  it('does not fabricate totals before the backend preview arrives', () => {
    renderReview({ preview: undefined, isPreviewLoading: false });
    expect(screen.queryByTestId('checkout-summary-total')).not.toBeInTheDocument();
    expect(screen.getAllByText('Order totals are calculated by the store.').length).toBeGreaterThan(0);
  });

  it('emits back and submit handlers', () => {
    const onBack = vi.fn();
    const onSubmit = vi.fn();
    renderReview({ onBack, onSubmit });
    screen.getByRole('button', { name: 'Back to Delivery' }).click();
    screen.getByRole('button', { name: 'Continue to Payment' }).click();
    expect(onBack).toHaveBeenCalledTimes(1);
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
});