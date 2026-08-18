import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CartDrawer } from '../CartDrawer';
import { cartApi } from '../../../../services/cartApi';
import { makeCartResponse } from '../../__tests__/fixtures';

vi.mock('../../../../services/cartApi', () => ({
  cartApi: {
    getCart: vi.fn(),
    addItem: vi.fn(),
    updateItemQuantity: vi.fn(),
    removeItem: vi.fn(),
    clearCart: vi.fn(),
  },
}));

const renderDrawer = (queryClient: QueryClient, props: Partial<React.ComponentProps<typeof CartDrawer>> = {}) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <CartDrawer isOpen onClose={vi.fn()} {...props} />
      </MemoryRouter>
    </QueryClientProvider>
  );
};

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('CartDrawer (FD-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('renders nothing when closed', () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    const { container } = renderDrawer(newClient(), { isOpen: false });
    expect(container.querySelector('.drawer-overlay')).not.toBeInTheDocument();
  });

  it('renders cart lines and the subtotal from the shared cache', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    renderDrawer(newClient());

    expect(await screen.findByTestId('cart-drawer-item')).toBeInTheDocument();
    expect(screen.getByText('Blue Oyster Mushroom Spawn')).toBeInTheDocument();
    expect(screen.getByTestId('cart-drawer-subtotal')).toHaveTextContent('$498.00');
    expect(screen.getByRole('link', { name: 'View Cart' })).toHaveAttribute('href', '/cart');
  });

  it('shows an empty state when the cart has no items', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 0, items: [], subtotal: 0 }));
    renderDrawer(newClient());

    expect(await screen.findByText('Your cart is empty.')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'View Cart' })).not.toBeInTheDocument();
  });

  it('closes via the drawer close button', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    const onClose = vi.fn();
    renderDrawer(newClient(), { onClose });

    fireEvent.click(await screen.findByRole('button', { name: 'Close drawer panel' }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});