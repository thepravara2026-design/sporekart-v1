import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { OrderSummary } from '../OrderSummary';
import { makeOrder } from '../../__tests__/fixtures';

describe('OrderSummary (FD-13)', () => {
  it('renders backend totals: items, subtotal, tax, shipping, total', () => {
    render(<OrderSummary order={makeOrder()} />);

    expect(screen.getByText('Order Summary')).toBeInTheDocument();
    expect(screen.getByText('₹498.00')).toBeInTheDocument(); // subtotal
    expect(screen.getByText('₹44.82')).toBeInTheDocument(); // tax
    expect(screen.getByText('₹25.00')).toBeInTheDocument(); // shipping
    expect(screen.getByTestId('order-summary')).toBeInTheDocument();
    expect(screen.getByText('₹567.82')).toBeInTheDocument(); // grand total
  });

  it('renders the discount line only when the backend reports a discount', () => {
    const { rerender } = render(<OrderSummary order={makeOrder({ discountTotal: 0 })} />);
    expect(screen.queryByText('Discount')).not.toBeInTheDocument();

    rerender(<OrderSummary order={makeOrder({ discountTotal: 50 })} />);
    expect(screen.getByText('Discount')).toBeInTheDocument();
    expect(screen.getByText('−₹50.00')).toBeInTheDocument();
  });

  it('uses the order currency for formatting', () => {
    render(<OrderSummary order={makeOrder({ currency: 'USD', grandTotal: 20 })} />);
    expect(screen.getByText('$20.00')).toBeInTheDocument();
  });
});