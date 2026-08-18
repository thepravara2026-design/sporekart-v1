import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CheckoutStepper } from '../CheckoutStepper';

describe('CheckoutStepper (FD-11)', () => {
  it('renders all steps with the current step marked and announced', () => {
    render(<CheckoutStepper currentStep={1} />);

    expect(screen.getByRole('navigation', { name: 'Checkout progress' })).toBeInTheDocument();
    expect(screen.getByText('Shipping')).toBeInTheDocument();
    expect(screen.getByText('Payment')).toBeInTheDocument();
    expect(screen.getByText('Review & Place Order')).toBeInTheDocument();

    const current = screen.getByTestId('checkout-step-1');
    expect(current).toHaveAttribute('aria-current', 'step');
    expect(screen.getByTestId('checkout-step-0')).not.toHaveAttribute('aria-current');
    expect(screen.getByTestId('checkout-step-2')).not.toHaveAttribute('aria-current');
  });

  it('marks completed steps as complete on the final step', () => {
    render(<CheckoutStepper currentStep={2} />);
    expect(screen.getByTestId('checkout-step-0')).not.toHaveAttribute('aria-current');
    expect(screen.getByTestId('checkout-step-2')).toHaveAttribute('aria-current', 'step');
  });
});