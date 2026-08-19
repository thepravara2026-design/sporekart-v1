import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CheckoutValidationAlert } from '../CheckoutValidationAlert';
import { CheckoutWarningResponse } from '../../../../services/cartApi';

const makeWarning = (type: string, overrides: Partial<CheckoutWarningResponse> = {}): CheckoutWarningResponse => ({
  type,
  productId: 'prod-1',
  message: 'Warning message',
  ...overrides,
});

describe('CheckoutValidationAlert (FD-12)', () => {
  it('renders nothing when there are no warnings or notices', () => {
    const { container } = render(<CheckoutValidationAlert warnings={[]} />);
    expect(container.querySelector('[data-testid="checkout-validation-alert"]')).toBeNull();
  });

  it('renders blocking warnings as errors with an actionable hint', () => {
    render(<CheckoutValidationAlert warnings={[makeWarning('ITEM_UNAVAILABLE', { message: 'This item sold out.' })]} />);
    expect(screen.getByText('This item sold out.')).toBeInTheDocument();
    expect(screen.getByText('Item no longer available')).toBeInTheDocument();
    expect(
      screen.getByText(/no longer available\. Return to your cart to review the items before proceeding\./)
    ).toBeInTheDocument();
  });

  it('renders price-change warnings as warnings without blocking the flow', () => {
    render(<CheckoutValidationAlert warnings={[makeWarning('PRICE_CHANGED', { message: 'Price moved.' })]} />);
    expect(screen.getByText('Price moved.')).toBeInTheDocument();
    expect(screen.getByText('Prices have changed')).toBeInTheDocument();
  });

  it('renders the revalidation notice when totals changed', () => {
    render(<CheckoutValidationAlert warnings={[]} revalidationNotice="The order total has changed. Please review your order." />);
    expect(screen.getByText('Order total changed')).toBeInTheDocument();
    expect(screen.getByText('The order total has changed. Please review your order.')).toBeInTheDocument();
  });
});
