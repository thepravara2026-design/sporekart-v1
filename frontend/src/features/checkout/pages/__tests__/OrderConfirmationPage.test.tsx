import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { OrderConfirmationPage } from '../OrderConfirmationPage';
import { orderApi, OrderDto } from '../../../../services/orderApi';
import { ToastProvider } from '../../../../components/ui/Toast';
import { ApiError } from '../../../../services/apiError';

vi.mock('../../../../services/orderApi', () => ({
  orderApi: {
    createOrder: vi.fn(),
    getOrderByReference: vi.fn(),
    getOrderHistory: vi.fn(),
  },
}));

const makeOrder = (overrides: Partial<OrderDto> = {}): OrderDto => ({
  id: 'order-1',
  orderNumber: 'ORD-2026-000001',
  customerId: 'cust-1',
  status: 'CONFIRMED',
  currency: 'INR',
  subtotal: 498,
  discountTotal: 0,
  taxTotal: 44.82,
  shippingFee: 25,
  grandTotal: 567.82,
  shippingAddress: {
    fullName: 'A. Buyer',
    phone: '+919876543210',
    addressLine1: '42 Fungal Lane',
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560001',
    country: 'India',
  },
  items: [
    {
      id: 'oi-1',
      orderId: 'order-1',
      productId: 'prod-1',
      sku: 'SKU-OYSTER-01',
      productNameSnapshot: 'Blue Oyster Spawn',
      unitPrice: 249,
      quantity: 2,
      discountAmount: 0,
      taxAmount: 44.82,
      lineSubtotal: 498,
      lineTotal: 542.82,
      createdAt: '2026-08-18T11:00:00Z',
    },
  ],
  createdAt: '2026-08-18T11:00:00Z',
  updatedAt: '2026-08-18T11:00:00Z',
  ...overrides,
});

const renderConfirmation = (queryClient: QueryClient, route: string) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <MemoryRouter initialEntries={[route]}>
          <Routes>
            <Route path="/checkout/confirmation" element={<OrderConfirmationPage />} />
          </Routes>
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
};

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('OrderConfirmationPage (FD-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('requires sign-in', () => {
    localStorage.removeItem('accessToken');
    renderConfirmation(newClient(), '/checkout/confirmation?orderNumber=ORD-2026-000001');
    expect(screen.getByText('Sign in required')).toBeInTheDocument();
  });

  it('shows a helpful state when no order number is present', () => {
    renderConfirmation(newClient(), '/checkout/confirmation');
    expect(screen.getByText('No order found')).toBeInTheDocument();
  });

  it('loads and renders the confirmed order detail', async () => {
    vi.mocked(orderApi.getOrderByReference).mockResolvedValue({ success: true, data: makeOrder() });

    renderConfirmation(newClient(), '/checkout/confirmation?orderNumber=ORD-2026-000001');

    expect(await screen.findByTestId('order-confirmation')).toBeInTheDocument();
    expect(orderApi.getOrderByReference).toHaveBeenCalledWith('ORD-2026-000001');
    expect(screen.getByTestId('order-confirmation-number')).toHaveTextContent('ORD-2026-000001');
    expect(screen.getByTestId('order-confirmation-status')).toHaveTextContent('Confirmed');
    expect(screen.getByTestId('order-confirmation-total')).toHaveTextContent('₹567.82');
    expect(screen.getByText('Blue Oyster Spawn')).toBeInTheDocument();
    expect(screen.getByText('42 Fungal Lane')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toHaveAttribute('href', '/products');
  });

  it('renders a pending status banner for non-confirmed orders', async () => {
    vi.mocked(orderApi.getOrderByReference).mockResolvedValue({
      success: true,
      data: makeOrder({ status: 'PAYMENT_PENDING' }),
    });

    renderConfirmation(newClient(), '/checkout/confirmation?orderNumber=ORD-2026-000001');

    expect(await screen.findByText('Order received')).toBeInTheDocument();
    expect(screen.getByTestId('order-confirmation-status')).toHaveTextContent('Payment Pending');
  });

  it('handles a missing or inaccessible order', async () => {
    vi.mocked(orderApi.getOrderByReference).mockRejectedValue(
      new ApiError('Order not found', 'ORDER_NOT_FOUND', 404)
    );

    renderConfirmation(newClient(), '/checkout/confirmation?orderNumber=ORD-0000-MISSING');

    expect(await screen.findByText('Unable to load your order')).toBeInTheDocument();
    expect(screen.getByText('Order not found. It may have been placed on another account.')).toBeInTheDocument();
  });
});