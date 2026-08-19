import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CartSummary } from '../CartSummary';
import { makeCart } from '../../__tests__/fixtures';

const renderSummary = (props: Partial<React.ComponentProps<typeof CartSummary>> = {}) => {
  return render(
    <MemoryRouter>
      <CartSummary cart={makeCart()} onClearCart={vi.fn()} {...props} />
    </MemoryRouter>
  );
};

describe('CartSummary (FD-11)', () => {
  it('renders backend-supplied item count, subtotal and total', () => {
    renderSummary();
    expect(screen.getByTestId('cart-summary-item-count')).toHaveTextContent('2');
    expect(screen.getByTestId('cart-summary-subtotal')).toHaveTextContent(/₹\s*498\.00/);
    expect(screen.getByTestId('cart-summary-total')).toHaveTextContent(/₹\s*498\.00/);
  });

  it('provides a Proceed to Checkout link into the /checkout flow', () => {
    renderSummary();
    expect(screen.getByRole('link', { name: 'Proceed to Checkout' })).toHaveAttribute('href', '/checkout');
  });

  it('provides a Continue Shopping link to the catalog', () => {
    renderSummary();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toHaveAttribute('href', '/products');
  });

  it('triggers the clear-cart action and reflects the pending state', () => {
    const onClearCart = vi.fn();
    const { rerender } = renderSummary({ onClearCart, isClearing: false });

    fireEvent.click(screen.getByTestId('clear-cart-trigger'));
    expect(onClearCart).toHaveBeenCalledTimes(1);

    rerender(
      <MemoryRouter>
        <CartSummary cart={makeCart()} onClearCart={onClearCart} isClearing />
      </MemoryRouter>
    );
    expect(screen.getByText('Clearing cart...')).toBeInTheDocument();
    expect(screen.getByTestId('clear-cart-trigger')).toBeDisabled();
  });
});
