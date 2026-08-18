import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CartPage } from '../CartPage';
import { cartApi } from '../../../../services/cartApi';
import { ApiError } from '../../../../services/apiError';
import { ToastProvider } from '../../../../components/ui/Toast';
import { makeCartResponse, makeCart } from '../../__tests__/fixtures';

vi.mock('../../../../services/cartApi', () => ({
  cartApi: {
    getCart: vi.fn(),
    addItem: vi.fn(),
    updateItemQuantity: vi.fn(),
    removeItem: vi.fn(),
    clearCart: vi.fn(),
  },
}));

const renderCartPage = (queryClient: QueryClient) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <MemoryRouter initialEntries={['/cart']}>
          <Routes>
            <Route path="/cart" element={<CartPage />} />
          </Routes>
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
};

const newClient = () =>
  new QueryClient({ defaultOptions: { queries: { retry: false } } });

/**
 * Stateful cart mock that mirrors backend persistence: successful mutations
 * update `current`, so the post-mutation cart refetch returns the same state
 * the mutation just returned (as a real backend would).
 */
const statefulCartMock = (initial = makeCartResponse()) => {
  let current = initial;
  vi.mocked(cartApi.getCart).mockImplementation(() => Promise.resolve(current));
  const setCurrent = (next: ReturnType<typeof makeCartResponse>) => {
    current = next;
  };
  return { setCurrent };
};

describe('CartPage (FD-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('requires sign-in and does not fetch the cart anonymously', () => {
    localStorage.clear();
    renderCartPage(newClient());
    expect(screen.getByText('Sign in required')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toBeInTheDocument();
    expect(cartApi.getCart).not.toHaveBeenCalled();
  });

  it('shows the loading skeleton while fetching', () => {
    vi.mocked(cartApi.getCart).mockReturnValue(new Promise(() => {}));
    renderCartPage(newClient());
    expect(screen.getByTestId('cart-loading')).toBeInTheDocument();
  });

  it('renders an empty cart with a Continue Shopping CTA', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 0, items: [], subtotal: 0 }));
    renderCartPage(newClient());
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Your cart is empty' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toHaveAttribute('href', '/products');
  });

  it('recovers from a fetch error via Retry', async () => {
    vi.mocked(cartApi.getCart)
      .mockRejectedValueOnce(new ApiError('Backend server is unavailable or network is disconnected.', 'NETWORK_ERROR'))
      .mockResolvedValueOnce(makeCartResponse({ itemCount: 2 }));
    renderCartPage(newClient());

    expect(await screen.findByTestId('cart-error')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));
    expect(await screen.findByTestId('cart-item')).toBeInTheDocument();
  });

  it('renders items, line totals, and the order summary from backend data', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');
    expect(screen.getByRole('link', { name: 'Blue Oyster Mushroom Spawn' })).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-OYSTER-01')).toBeInTheDocument();
    expect(screen.getByText('Line total: $498.00')).toBeInTheDocument();
    expect(screen.getByTestId('cart-summary-item-count')).toHaveTextContent('2');
    expect(screen.getByTestId('cart-summary-total')).toHaveTextContent('$498.00');
    expect(screen.getByRole('link', { name: 'Proceed to Checkout' })).toHaveAttribute('href', '/checkout');
  });

  it('updates quantity through the backend and reflects the server-confirmed cart', async () => {
    const { setCurrent } = statefulCartMock();
    const updated = makeCartResponse({
      itemCount: 3,
      subtotal: 747,
      items: [{ ...makeCartResponse().data.items[0], quantity: 3, lineTotal: 747 }],
    });
    vi.mocked(cartApi.updateItemQuantity).mockImplementation(async () => {
      setCurrent(updated);
      return updated;
    });
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Mushroom Spawn' }));

    await waitFor(() => {
      expect(cartApi.updateItemQuantity).toHaveBeenCalledWith('item-1', { quantity: 3 });
    });
    await waitFor(() => {
      expect(screen.getByText('Line total: $747.00')).toBeInTheDocument();
      expect(screen.getByTestId('cart-summary-total')).toHaveTextContent('$747.00');
    });
  });

  it('does not claim success when a quantity update fails; value and totals are preserved', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    vi.mocked(cartApi.updateItemQuantity).mockRejectedValue(
      new ApiError('Quantity must be at least 1', 'CART_INVALID_QUANTITY', 400)
    );
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');
    fireEvent.click(screen.getByRole('button', { name: 'Decrease quantity for Blue Oyster Mushroom Spawn' }));

    const error = await screen.findByText('Unable to update quantity. The requested quantity is not allowed.');
    expect(error).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Quantity for Blue Oyster Mushroom Spawn' })).toHaveValue(2);
    expect(screen.getByTestId('cart-summary-total')).toHaveTextContent('$498.00');
  });

  it('flags a line as unavailable when the backend rejects its product, keeping remove available', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    vi.mocked(cartApi.updateItemQuantity).mockRejectedValue(
      new ApiError('Product status is OUT_OF_STOCK', 'CATALOG_PRODUCT_NOT_PURCHASABLE', 400)
    );
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Mushroom Spawn' }));

    expect(await screen.findByTestId('cart-item-unavailable')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Mushroom Spawn' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' })).toBeEnabled();
  });

  it('removes an item through the backend and transitions to the empty state', async () => {
    const { setCurrent } = statefulCartMock();
    const empty = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
    vi.mocked(cartApi.removeItem).mockImplementation(async () => {
      setCurrent(empty);
      return empty;
    });
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');
    fireEvent.click(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' }));

    await waitFor(() => {
      expect(cartApi.removeItem).toHaveBeenCalledWith('item-1');
    });
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
  });

  it('reconciles with the backend when a removal targets a missing item', async () => {
    vi.mocked(cartApi.getCart)
      .mockResolvedValueOnce(makeCartResponse())
      .mockResolvedValueOnce(makeCartResponse({ itemCount: 0, items: [], subtotal: 0 }));
    vi.mocked(cartApi.removeItem).mockRejectedValue(
      new ApiError('Cart item not found', 'CART_ITEM_NOT_FOUND', 404)
    );
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');
    fireEvent.click(screen.getByRole('button', { name: 'Remove Blue Oyster Mushroom Spawn from cart' }));

    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
    expect(cartApi.getCart).toHaveBeenCalledTimes(2);
  });

  it('clears the cart only after confirmation from the dialog', async () => {
    const { setCurrent } = statefulCartMock();
    const empty = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
    vi.mocked(cartApi.clearCart).mockImplementation(async () => {
      setCurrent(empty);
      return empty;
    });
    renderCartPage(newClient());

    await screen.findByTestId('cart-item');

    fireEvent.click(screen.getByTestId('clear-cart-trigger'));
    expect(await screen.findByRole('dialog', { name: 'Clear your cart?' })).toBeInTheDocument();
    expect(cartApi.clearCart).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: 'Clear Cart' }));
    await waitFor(() => {
      expect(cartApi.clearCart).toHaveBeenCalledTimes(1);
    });
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
  });

  it('renders exactly one H1 on the cart page', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse());
    renderCartPage(newClient());
    await screen.findByTestId('cart-item');
    const headings = screen.getAllByRole('heading', { level: 1 });
    expect(headings).toHaveLength(1);
    expect(headings[0]).toHaveTextContent('Your Cart');
  });

  it('renders a multi-item cart with per-line controls', async () => {
    const cart = makeCart({
      itemCount: 5,
      subtotal: 996,
      items: [
        { ...makeCartResponse().data.items[0] },
        { ...makeCartResponse().data.items[0], id: 'item-2', productName: 'Lion\u2019s Mane Spawn', sku: 'SKU-LION-01', quantity: 3, lineTotal: 747 },
      ],
    });
    vi.mocked(cartApi.getCart).mockResolvedValue({ success: true, data: cart });
    renderCartPage(newClient());

    await screen.findByTestId('cart-layout');
    const items = screen.getAllByTestId('cart-item');
    expect(items).toHaveLength(2);
    expect(screen.getByRole('link', { name: 'Lion\u2019s Mane Spawn' })).toBeInTheDocument();
    expect(screen.getByTestId('cart-summary-item-count')).toHaveTextContent('5');
  });
});