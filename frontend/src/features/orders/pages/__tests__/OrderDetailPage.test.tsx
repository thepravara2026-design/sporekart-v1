import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { OrderDetailPage } from '../OrderDetailPage';
import { orderApi, OrderDto } from '../../../../services/orderApi';
import { shippingApi } from '../../../../services/shippingApi';
import { returnApi } from '../../../../services/returnApi';
import { ToastProvider } from '../../../../components/ui/Toast';
import { makeOrder, makeOrderTimeline, makeShipment, makeShipmentTracking, makeReturnEligibility } from '../../__tests__/fixtures';
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

vi.mock('../../../../services/shippingApi', () => ({
  shippingApi: {
    getCustomerShipment: vi.fn(),
    getCustomerTracking: vi.fn(),
  },
}));

vi.mock('../../../../services/returnApi', () => ({
  returnApi: { checkEligibility: vi.fn() },
}));

const renderDetail = (queryClient: QueryClient, reference = 'ORD-2026-000001') =>
  render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <MemoryRouter initialEntries={[`/orders/${reference}`]}>
          <Routes>
            <Route path="/orders/:orderReference" element={<OrderDetailPage />} />
          </Routes>
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>
  );

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('OrderDetailPage (FD-13)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
    vi.mocked(orderApi.getOrderByReference).mockResolvedValue({ success: true, data: makeOrder() });
    vi.mocked(orderApi.getOrderTimeline).mockResolvedValue({ success: true, data: makeOrderTimeline() });
    vi.mocked(returnApi.checkEligibility).mockResolvedValue(makeReturnEligibility({ eligible: false }));
  });

  it('requires sign-in', () => {
    localStorage.removeItem('accessToken');
    renderDetail(newClient());
    expect(screen.getByText('Sign in required')).toBeInTheDocument();
    expect(orderApi.getOrderByReference).not.toHaveBeenCalled();
  });

  it('renders order header, items, summary, and address', async () => {
    renderDetail(newClient());

    expect(await screen.findByTestId('order-detail-page')).toBeInTheDocument();
    expect(screen.getByText('ORD-2026-000001')).toBeInTheDocument();
    // "Confirmed" appears as the status badge and as the latest timeline event.
    expect(screen.getAllByText('Confirmed').length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText('Blue Oyster Spawn')).toBeInTheDocument();
    expect(screen.getByText('42 Fungal Lane')).toBeInTheDocument();
    expect(screen.getByText('₹567.82')).toBeInTheDocument();
    expect(orderApi.getOrderByReference).toHaveBeenCalledWith('ORD-2026-000001');
  });

  it('renders the status timeline when available', async () => {
    renderDetail(newClient());

    expect(await screen.findByTestId('order-timeline')).toBeInTheDocument();
    expect(screen.getByText('Order Timeline')).toBeInTheDocument();
  });

  it('shows the Cancel action for cancellable statuses', async () => {
    vi.mocked(orderApi.getOrderByReference).mockResolvedValue({
      success: true,
      data: makeOrder({ status: 'PAYMENT_PENDING' }),
    });

    renderDetail(newClient());
    expect(await screen.findByRole('button', { name: 'Cancel Order' })).toBeInTheDocument();
  });

  it('hides the Cancel action for non-cancellable statuses', async () => {
    vi.mocked(orderApi.getOrderByReference).mockResolvedValue({
      success: true,
      data: makeOrder({ status: 'DELIVERED' }),
    });

    renderDetail(newClient());
    expect(await screen.findByTestId('order-detail-page')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Cancel Order' })).not.toBeInTheDocument();
  });

  it('cancels an order via the confirmation dialog and refreshes state', async () => {
    let current: OrderDto = makeOrder({ status: 'PAYMENT_PENDING' });
    vi.mocked(orderApi.getOrderByReference).mockImplementation(() =>
      Promise.resolve({ success: true, data: current })
    );
    vi.mocked(orderApi.cancelOrder).mockImplementation(() => {
      current = makeOrder({ status: 'CANCELLED' });
      return Promise.resolve({ success: true, data: current });
    });

    renderDetail(newClient());

    fireEvent.click(await screen.findByRole('button', { name: 'Cancel Order' }));
    expect(screen.getByText('Cancel this order?')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Reason (optional)'), { target: { value: 'Changed my mind' } });
    fireEvent.click(screen.getByRole('button', { name: 'Confirm Cancellation' }));

    await waitFor(() =>
      expect(orderApi.cancelOrder).toHaveBeenCalledWith('ORD-2026-000001', 'Changed my mind')
    );
    expect(await screen.findByText('Cancelled')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Cancel Order' })).not.toBeInTheDocument();
  });

  it('renders shipment and tracking sections for shipped orders', async () => {
    vi.mocked(orderApi.getOrderByReference).mockResolvedValue({
      success: true,
      data: makeOrder({ status: 'SHIPPED' }),
    });
    vi.mocked(shippingApi.getCustomerShipment).mockResolvedValue(makeShipment());
    vi.mocked(shippingApi.getCustomerTracking).mockResolvedValue(makeShipmentTracking());

    renderDetail(newClient());

    expect(await screen.findByTestId('shipment-status')).toBeInTheDocument();
    expect(screen.getByText('Delhivery')).toBeInTheDocument();
    expect(screen.getByTestId('shipment-tracking')).toBeInTheDocument();
    expect(screen.getByText('Shipment picked up')).toBeInTheDocument();
    expect(shippingApi.getCustomerShipment).toHaveBeenCalledWith('ORD-2026-000001');
    expect(shippingApi.getCustomerTracking).toHaveBeenCalledWith('ORD-2026-000001');
  });

  it('does not query shipment data for orders that have no shipment', async () => {
    renderDetail(newClient()); // CONFIRMED — not shipment-relevant

    expect(await screen.findByTestId('order-detail-page')).toBeInTheDocument();
    expect(shippingApi.getCustomerShipment).not.toHaveBeenCalled();
    expect(shippingApi.getCustomerTracking).not.toHaveBeenCalled();
  });

  it('surfaces the return handoff when eligible', async () => {
    vi.mocked(returnApi.checkEligibility).mockResolvedValue(makeReturnEligibility({ eligible: true }));

    renderDetail(newClient());

    const link = await screen.findByRole('link', { name: 'Request Return' });
    expect(link).toHaveAttribute('href', '/orders/ORD-2026-000001/return-request');
  });

  it('shows a friendly error state when the order cannot be loaded', async () => {
    vi.mocked(orderApi.getOrderByReference).mockRejectedValue(
      new ApiError('Order not found', 'ORDER_NOT_FOUND', 404)
    );

    renderDetail(newClient());

    expect(await screen.findByText('Unable to load this order')).toBeInTheDocument();
    expect(screen.getByText(/Order not found/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Back to My Orders' })).toHaveAttribute('href', '/orders');
  });
});