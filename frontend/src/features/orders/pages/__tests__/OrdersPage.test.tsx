import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { OrdersPage } from '../OrdersPage';
import { orderApi } from '../../../../services/orderApi';
import { makeOrderSummary, makeOrderPage } from '../../__tests__/fixtures';
import { ApiError } from '../../../../services/apiError';

vi.mock('../../../../services/orderApi', () => ({
  orderApi: {
    createOrder: vi.fn(),
    getOrderByReference: vi.fn(),
    getOrderHistory: vi.fn(),
    getOrderTimeline: vi.fn(),
    cancelOrder: vi.fn(),
  },
}));

const renderOrdersPage = (queryClient: QueryClient) =>
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/orders']}>
        <Routes>
          <Route path="/orders" element={<OrdersPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('OrdersPage (FD-13)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('requires sign-in and does not fetch history anonymously', () => {
    localStorage.removeItem('accessToken');
    renderOrdersPage(newClient());

    expect(screen.getByText('Sign in required')).toBeInTheDocument();
    expect(orderApi.getOrderHistory).not.toHaveBeenCalled();
  });

  it('renders the paginated order history as order cards', async () => {
    const page = makeOrderPage([makeOrderSummary({ status: 'DELIVERED' }), makeOrderSummary({
      id: 'order-2',
      orderNumber: 'ORD-2026-000002',
      status: 'CANCELLED',
    })]);
    vi.mocked(orderApi.getOrderHistory).mockResolvedValue({ success: true, data: page });

    renderOrdersPage(newClient());

    const cards = await screen.findAllByTestId('order-card');
    expect(cards).toHaveLength(2);
    expect(orderApi.getOrderHistory).toHaveBeenCalledWith(0, 10);
    expect(within(cards[0]).getByText('Delivered')).toBeInTheDocument();
    expect(within(cards[1]).getByText('Cancelled')).toBeInTheDocument();
  });

  it('renders the empty state when the customer has no orders', async () => {
    vi.mocked(orderApi.getOrderHistory).mockResolvedValue({ success: true, data: makeOrderPage([]) });

    renderOrdersPage(newClient());

    expect(await screen.findByText('No orders yet')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Start Shopping' })).toHaveAttribute('href', '/products');
  });

  it('shows an error state and retries', async () => {
    vi.mocked(orderApi.getOrderHistory).mockRejectedValue(new ApiError('nope', 'ORDER_NOT_FOUND', 404));

    renderOrdersPage(newClient());

    expect(await screen.findByTestId('orders-error')).toBeInTheDocument();

    vi.mocked(orderApi.getOrderHistory).mockResolvedValue({ success: true, data: makeOrderPage([]) });
    fireEvent.click(screen.getByRole('button', { name: 'Try Again' }));

    await waitFor(() => expect(orderApi.getOrderHistory).toHaveBeenCalledTimes(2));
  });

  it('paginates with zero-indexed pages and a status filter resets to page 0', async () => {
    const page0 = makeOrderPage(
      Array.from({ length: 10 }, (_, i) => makeOrderSummary({ id: `o-${i}`, orderNumber: `ORD-2026-${i}` })),
      { totalPages: 2, last: false }
    );
    const page1 = makeOrderPage(
      [makeOrderSummary({ id: 'o-10', orderNumber: 'ORD-2026-10' })],
      { page: 1, first: false, last: true, totalPages: 2 }
    );
    vi.mocked(orderApi.getOrderHistory).mockImplementation((page) =>
      Promise.resolve({ success: true, data: page === 0 ? page0 : page1 })
    );

    renderOrdersPage(newClient());

    expect(await screen.findAllByTestId('order-card')).toHaveLength(10);
    fireEvent.click(screen.getByRole('button', { name: 'Go to page 2' }));
    expect(await screen.findByText('ORD-2026-10')).toBeInTheDocument();
    expect(orderApi.getOrderHistory).toHaveBeenLastCalledWith(1, 10);

    // Changing the filter resets to page 0.
    fireEvent.change(screen.getByLabelText('Filter orders by status'), { target: { value: 'DELIVERED' } });
    await waitFor(() => expect(orderApi.getOrderHistory).toHaveBeenLastCalledWith(0, 10));
  });
});
