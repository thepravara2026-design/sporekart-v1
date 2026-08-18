import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CheckoutPaymentForm } from '../CheckoutPaymentForm';
import { CheckoutPreviewResponse } from '../../../../services/cartApi';

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
  breakdown: {
    subtotal: 498,
    discountTotal: 0,
    taxTotal: 44.82,
    shippingFee: 25,
    grandTotal: 567.82,
    currency: 'INR',
  },
  warnings: [],
  generatedAt: '2026-08-18T11:00:00Z',
  ...overrides,
});

describe('CheckoutPaymentForm (FD-12)', () => {
  it('renders payment methods, order summary totals, and the place-order CTA', () => {
    render(
      <CheckoutPaymentForm preview={makePreview()} onSubmit={vi.fn()} onBack={vi.fn()} />
    );

    expect(screen.getByRole('radio', { name: /UPI/ })).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: /Credit \/ Debit Card/ })).toBeInTheDocument();
    expect(screen.getByTestId('checkout-summary-subtotal')).toHaveTextContent('₹498.00');
    expect(screen.getByTestId('checkout-summary-tax')).toHaveTextContent('₹44.82');
    expect(screen.getByTestId('checkout-summary-shipping')).toHaveTextContent('₹25.00');
    expect(screen.getByTestId('checkout-summary-total')).toHaveTextContent('₹567.82');
    expect(screen.getByRole('button', { name: 'Place Order & Pay' })).toBeInTheDocument();
  });

  it('submits with the selected payment method', () => {
    const onSubmit = vi.fn();
    render(<CheckoutPaymentForm preview={makePreview()} onSubmit={onSubmit} onBack={vi.fn()} />);

    fireEvent.click(screen.getByRole('radio', { name: /Credit \/ Debit Card/ }));
    fireEvent.click(screen.getByRole('button', { name: 'Place Order & Pay' }));

    expect(onSubmit).toHaveBeenCalledWith('CARD');
  });

  it('surfaces server checkout warnings before committing', () => {
    render(
      <CheckoutPaymentForm
        preview={makePreview({
          warnings: [
            { type: 'PRICE_CHANGED', productId: 'prod-1', message: 'The price of this item changed since you added it.' },
          ],
        })}
        onSubmit={vi.fn()}
        onBack={vi.fn()}
      />
    );

    expect(screen.getByText('The price of this item changed since you added it.')).toBeInTheDocument();
  });

  it('shows a status note while the server preview is computing', () => {
    render(
      <CheckoutPaymentForm preview={undefined} isPreviewLoading onSubmit={vi.fn()} onBack={vi.fn()} />
    );
    expect(screen.getByText('Calculating your order totals...')).toBeInTheDocument();
  });

  it('disables controls while the order is being placed', () => {
    render(
      <CheckoutPaymentForm preview={makePreview()} isPlacingOrder onSubmit={vi.fn()} onBack={vi.fn()} />
    );
    expect(screen.getByRole('button', { name: /Loading/ })).toBeDisabled();
    expect(screen.getByRole('radio', { name: /UPI/ })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Back to Review' })).toBeDisabled();
  });
});