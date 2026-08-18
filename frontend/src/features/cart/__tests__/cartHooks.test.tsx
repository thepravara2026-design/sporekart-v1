import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { FC, ReactNode } from 'react';
import { cartApi } from '../../../services/cartApi';
import { useCart } from '../hooks/useCart';
import { useCartCount } from '../hooks/useCartCount';
import { useUpdateCartItem } from '../hooks/useUpdateCartItem';
import { useRemoveCartItem } from '../hooks/useRemoveCartItem';
import { useClearCart } from '../hooks/useClearCart';
import { useAddToCart } from '../../catalog/hooks/useAddToCart';
import { ToastProvider } from '../../../components/ui/Toast';
import { ApiError } from '../../../services/apiError';
import { makeCartResponse } from './fixtures';

vi.mock('../../../services/cartApi', () => ({
  cartApi: {
    getCart: vi.fn(),
    addItem: vi.fn(),
    updateItemQuantity: vi.fn(),
    removeItem: vi.fn(),
    clearCart: vi.fn(),
  },
}));

const makeWrapper = (queryClient: QueryClient) =>
  (({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>{children}</ToastProvider>
    </QueryClientProvider>
  )) as FC<{ children: ReactNode }>;

describe('cart hooks (FD-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('useCart loads a populated cart and exposes the item count', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 4 }));
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    const Probe = () => {
      const { data, isLoading } = useCart();
      return (
        <div>
          <span data-testid="loading">{String(isLoading)}</span>
          <span data-testid="count">{data?.data.itemCount ?? -1}</span>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    expect(screen.getByTestId('count')).toHaveTextContent('-1');
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('4'));
  });

  it('useCart returns an empty cart with a zero count', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 0, items: [], subtotal: 0 }));
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    const Probe = () => {
      const { data } = useCart();
      return <span data-testid="count">{data?.data.itemCount ?? -1}</span>;
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('0'));
  });

  it('useCart surfaces a network error that clears on retry', async () => {
    vi.mocked(cartApi.getCart)
      .mockRejectedValueOnce(new ApiError('Backend server is unavailable or network is disconnected.', 'NETWORK_ERROR'))
      .mockResolvedValueOnce(makeCartResponse({ itemCount: 2 }));
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    const Probe = () => {
      const { data, isError, refetch } = useCart();
      return (
        <div>
          <span data-testid="error">{String(isError)}</span>
          <span data-testid="count">{data?.data.itemCount ?? -1}</span>
          <button onClick={() => refetch()}>retry</button>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    await waitFor(() => expect(screen.getByTestId('error')).toHaveTextContent('true'));
    screen.getByRole('button', { name: 'retry' }).click();
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('2'));
  });

  it('cart count stays synchronized after add, quantity update, remove, and clear', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    let current = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
    vi.mocked(cartApi.getCart).mockImplementation(() => Promise.resolve(current));
    vi.mocked(cartApi.addItem).mockImplementation(async () => {
      current = makeCartResponse({ itemCount: 2, subtotal: 498 });
      return current;
    });
    vi.mocked(cartApi.updateItemQuantity).mockImplementation(async () => {
      current = makeCartResponse({ itemCount: 3, subtotal: 747 });
      return current;
    });
    vi.mocked(cartApi.removeItem).mockImplementation(async () => {
      current = makeCartResponse({ itemCount: 1, subtotal: 249 });
      return current;
    });
    vi.mocked(cartApi.clearCart).mockImplementation(async () => {
      current = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
      return current;
    });

    const Probe = () => {
      const count = useCartCount();
      const add = useAddToCart();
      const update = useUpdateCartItem();
      const remove = useRemoveCartItem();
      const clear = useClearCart();
      return (
        <div>
          <span data-testid="count">{count}</span>
          <button data-testid="add" onClick={() => add.mutate({ productId: 'prod-1', quantity: 2 })}>
            add
          </button>
          <button data-testid="update" onClick={() => update.mutate({ itemId: 'item-1', quantity: 3 })}>
            update
          </button>
          <button data-testid="remove" onClick={() => remove.mutate('item-1')}>
            remove
          </button>
          <button data-testid="clear" onClick={() => clear.mutate(undefined)}>
            clear
          </button>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });

    // Initial cart fetch resolves to 0.
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('0'));

    // Add to cart → count 2 (cache written by useAddToCart, then refetched).
    screen.getByTestId('add').click();
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('2'));

    // Quantity update → count 3.
    screen.getByTestId('update').click();
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('3'));

    // Remove → count 1.
    screen.getByTestId('remove').click();
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('1'));

    // Clear → count 0.
    screen.getByTestId('clear').click();
    await waitFor(() => expect(screen.getByTestId('count')).toHaveTextContent('0'));

    expect(cartApi.addItem).toHaveBeenCalledWith({ productId: 'prod-1', quantity: 2 });
    expect(cartApi.updateItemQuantity).toHaveBeenCalledWith('item-1', { quantity: 3 });
    expect(cartApi.removeItem).toHaveBeenCalledWith('item-1');
    expect(cartApi.clearCart).toHaveBeenCalled();
  });

  it('a failed quantity mutation never updates the cache', async () => {
    vi.mocked(cartApi.updateItemQuantity).mockRejectedValue(
      new ApiError('Quantity out of range', 'CART_INVALID_QUANTITY', 400)
    );
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    queryClient.setQueryData(['cart'], makeCartResponse({ itemCount: 2 }));

    const Probe = () => {
      const mutation = useUpdateCartItem();
      return (
        <div>
          <span data-testid="error">{mutation.error ? 'error' : 'none'}</span>
          <button onClick={() => mutation.mutate({ itemId: 'item-1', quantity: 99 })}>update</button>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    screen.getByRole('button', { name: 'update' }).click();
    await waitFor(() => expect(screen.getByTestId('error')).toHaveTextContent('error'));

    // Cache still holds the last server-confirmed value.
    const cached = queryClient.getQueryData<{ data: { itemCount: number } }>(['cart']);
    expect(cached?.data.itemCount).toBe(2);
  });
});