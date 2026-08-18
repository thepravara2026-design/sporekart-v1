import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CheckoutSubmit } from '../CheckoutSubmit';
import { PAYMENT_METHODS } from '../../constants/checkoutConstants';

const renderSubmit = (props: Partial<React.ComponentProps<typeof CheckoutSubmit>> = {}) =>
  render(
    <MemoryRouter>
      <CheckoutSubmit onSubmit={vi.fn()} onBack={vi.fn()} {...props} />
    </MemoryRouter>
  );

describe('CheckoutSubmit (FD-12)', () => {
  it('renders the supported payment methods and the place-order CTA', () => {
    renderSubmit();
    for (const method of PAYMENT_METHODS) {
      expect(screen.getByRole('radio', { name: new RegExp(method.label) })).toBeInTheDocument();
    }
    expect(screen.getByRole('button', { name: 'Place Order & Pay' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Back to Review' })).toBeInTheDocument();
  });

  it('submits the selected payment method', () => {
    const onSubmit = vi.fn();
    renderSubmit({ onSubmit });
    fireEvent.click(screen.getByRole('radio', { name: /Credit \/ Debit Card/ }));
    fireEvent.click(screen.getByRole('button', { name: 'Place Order & Pay' }));
    expect(onSubmit).toHaveBeenCalledWith('CARD');
  });

  it('defaults to the first payment method (UPI)', () => {
    const onSubmit = vi.fn();
    renderSubmit({ onSubmit });
    fireEvent.click(screen.getByRole('button', { name: 'Place Order & Pay' }));
    expect(onSubmit).toHaveBeenCalledWith('UPI');
  });

  it('does not submit while an order is being placed and disables controls', () => {
    const onSubmit = vi.fn();
    renderSubmit({ isPlacingOrder: true, onSubmit });
    fireEvent.click(screen.getByRole('button', { name: /Loading/ }));
    expect(onSubmit).not.toHaveBeenCalled();
    expect(screen.getByRole('radio', { name: /UPI/ })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Back to Review' })).toBeDisabled();
  });
});