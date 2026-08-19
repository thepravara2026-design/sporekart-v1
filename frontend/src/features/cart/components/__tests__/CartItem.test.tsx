import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CartItem } from '../CartItem';
import { makeCartItem } from '../../__tests__/fixtures';

const renderItem = (props: Partial<React.ComponentProps<typeof CartItem>> = {}) => {
  const item = makeCartItem();
  return render(
    <MemoryRouter>
      <CartItem
        item={item}
        currency="INR"
        onQuantityChange={vi.fn()}
        onRemove={vi.fn()}
        {...props}
      />
    </MemoryRouter>
  );
};

describe('CartItem (FD-11)', () => {
  it('renders product image, name link, SKU, prices, quantity and remove', () => {
    renderItem();
    expect(screen.getByTestId('cart-item-image')).toBeInTheDocument();
    const nameLink = screen.getByRole('link', { name: 'Blue Oyster Mushroom Spawn' });
    expect(nameLink).toHaveAttribute('href', '/products/prod-1');
    expect(screen.getByText('SKU: SKU-OYSTER-01')).toBeInTheDocument();
    expect(screen.getByText(/₹\s*249\.00/)).toBeInTheDocument();
    expect(screen.getByText('each')).toBeInTheDocument();
    expect(screen.getByText(/Line total: ₹\s*498\.00/)).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Quantity for Blue Oyster Mushroom Spawn' })).toHaveValue(2);
    expect(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' })).toBeInTheDocument();
  });

  it('propagates quantity changes and removal to the page handlers', () => {
    const onQuantityChange = vi.fn();
    const onRemove = vi.fn();
    renderItem({ onQuantityChange, onRemove });

    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Mushroom Spawn' }));
    expect(onQuantityChange).toHaveBeenCalledWith(3);

    fireEvent.click(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' }));
    expect(onRemove).toHaveBeenCalled();
  });

  it('disables remove and quantity while the line removal is pending', () => {
    renderItem({ isRemoving: true });
    expect(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Mushroom Spawn' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Decrease quantity for Blue Oyster Mushroom Spawn' })).toBeDisabled();
  });

  it('renders an unavailable state, blocks quantity, but keeps remove available', () => {
    renderItem({ unavailable: true, unavailableMessage: 'This product is no longer available for purchase.' });
    expect(screen.getByTestId('cart-item-unavailable')).toBeInTheDocument();
    expect(screen.getByText('Blue Oyster Mushroom Spawn is unavailable')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Mushroom Spawn' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' })).toBeEnabled();
  });

  it('renders a per-item inline error for failed server operations', () => {
    renderItem({ error: 'Unable to update quantity. The requested quantity is not allowed.' });
    expect(screen.getByRole('alert')).toHaveTextContent('Unable to update quantity');
  });
});
