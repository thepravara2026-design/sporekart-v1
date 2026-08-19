import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CartItemQuantity } from '../CartItemQuantity';

describe('CartItemQuantity (FD-11)', () => {
  const productName = 'Blue Oyster Mushroom Spawn';

  it('renders product-specific quantity controls and current value', () => {
    render(<CartItemQuantity productName={productName} quantity={2} onQuantityChange={() => {}} />);
    expect(screen.getByRole('button', { name: `Decrease quantity for ${productName}` })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: `Increase quantity for ${productName}` })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: `Quantity for ${productName}` })).toHaveValue(2);
  });

  it('increments and decrements through the callback', () => {
    const onChange = vi.fn();
    render(<CartItemQuantity productName={productName} quantity={2} onQuantityChange={onChange} />);

    fireEvent.click(screen.getByRole('button', { name: `Increase quantity for ${productName}` }));
    expect(onChange).toHaveBeenLastCalledWith(3);

    fireEvent.click(screen.getByRole('button', { name: `Decrease quantity for ${productName}` }));
    expect(onChange).toHaveBeenLastCalledWith(1);
  });

  it('disables decrement at the minimum quantity', () => {
    render(<CartItemQuantity productName={productName} quantity={1} onQuantityChange={() => {}} />);
    expect(screen.getByRole('button', { name: `Decrease quantity for ${productName}` })).toBeDisabled();
  });

  it('disables increment at the maximum quantity', () => {
    render(<CartItemQuantity productName={productName} quantity={50} onQuantityChange={() => {}} />);
    expect(screen.getByRole('button', { name: `Increase quantity for ${productName}` })).toBeDisabled();
  });

  it('blocks all controls while a server-backed update is pending', () => {
    const onChange = vi.fn();
    render(
      <CartItemQuantity productName={productName} quantity={3} onQuantityChange={onChange} isPending />
    );

    fireEvent.click(screen.getByRole('button', { name: `Increase quantity for ${productName}` }));
    fireEvent.click(screen.getByRole('button', { name: `Decrease quantity for ${productName}` }));
    expect(onChange).not.toHaveBeenCalled();
    expect(screen.getByRole('spinbutton', { name: `Quantity for ${productName}` })).toBeDisabled();
    expect(screen.getByTestId('cart-item-quantity')).toHaveAttribute('aria-busy', 'true');
    expect(screen.getByText('Updating...')).toBeInTheDocument();
  });

  it('exposes 40px touch targets on the stepper controls', () => {
    const { container } = render(
      <CartItemQuantity productName={productName} quantity={2} onQuantityChange={() => {}} />
    );
    const input = screen.getByRole('spinbutton', { name: `Quantity for ${productName}` });
    expect(input).toHaveStyle({ height: '40px', width: '64px' });

    const buttons = container.querySelectorAll('button');
    buttons.forEach((btn) => {
      expect(btn.style.height).toBe('40px');
      expect(btn.style.width).toBe('40px');
    });
  });
});
