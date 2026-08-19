import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CheckoutSummary } from '../CheckoutSummary';
import { PriceBreakdownResponse } from '../../../../services/cartApi';

const makeBreakdown = (overrides: Partial<PriceBreakdownResponse> = {}): PriceBreakdownResponse => ({
  subtotal: 498,
  discountTotal: 0,
  taxTotal: 44.82,
  shippingFee: 25,
  grandTotal: 567.82,
  currency: 'INR',
  ...overrides,
});

describe('CheckoutSummary (FD-12)', () => {
  it('renders all backend totals with the item count', () => {
    render(<CheckoutSummary breakdown={makeBreakdown()} itemCount={2} />);

    expect(screen.getByTestId('checkout-summary-subtotal')).toHaveTextContent('₹498.00');
    expect(screen.getByTestId('checkout-summary-tax')).toHaveTextContent('₹44.82');
    expect(screen.getByTestId('checkout-summary-shipping')).toHaveTextContent('₹25.00');
    expect(screen.getByTestId('checkout-summary-total')).toHaveTextContent('₹567.82');
    expect(screen.getByText('Items (2)')).toBeInTheDocument();
  });

  it('shows the discount row only when the backend reports one', () => {
    const { rerender } = render(<CheckoutSummary breakdown={makeBreakdown()} />);
    expect(screen.queryByTestId('checkout-summary-discount')).not.toBeInTheDocument();

    rerender(<CheckoutSummary breakdown={makeBreakdown({ discountTotal: 20 })} />);
    expect(screen.getByTestId('checkout-summary-discount')).toHaveTextContent('₹20.00');
  });

  it('omits the item count when not provided', () => {
    render(<CheckoutSummary breakdown={makeBreakdown()} />);
    expect(screen.queryByText(/Items \(\d+\)/)).not.toBeInTheDocument();
  });

  it('never displays a zero discount row', () => {
    render(<CheckoutSummary breakdown={makeBreakdown({ discountTotal: 0 })} />);
    expect(screen.queryByTestId('checkout-summary-discount')).not.toBeInTheDocument();
  });
});
