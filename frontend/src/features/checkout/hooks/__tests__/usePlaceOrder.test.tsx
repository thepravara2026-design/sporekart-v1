import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { FC, ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { orderApi, OrderDto, AddressDto } from '../../../../services/orderApi';
import { paymentApi, PaymentCheckoutDto, PaymentDto } from '../../../../services/paymentApi';
import { inventoryApi, ReservationDto } from '../../../../services/inventoryApi';
import { ApiError } from '../../../../services/apiError';
import { usePlaceOrder } from '../usePlaceOrder';

vi.mock('../../../../services/orderApi', () => ({
  orderApi: {
    createOrder: vi.fn(),
    getOrderByReference: vi.fn(),
    getOrderHistory: vi.fn(),
  },
}));

vi.mock('../../../../services/paymentApi', () => ({
  paymentApi: {
    initiatePayment: vi.fn(),
    verifyPayment: vi.fn(),
    getPaymentByReference: vi.fn(),
  },
}));

vi.mock('../../../../services/inventoryApi', () => ({
  inventoryApi: {
    reserveInventory: vi.fn(),
    releaseReservation: vi.fn(),
    getAvailability: vi.fn(),
  },
}));

const makeAddress = (): AddressDto => ({
  fullName: 'A. Buyer',
  phone: '+919876543210',
  addressLine1: '42 Fungal Lane',
  addressLine2: '',
  city: 'Bengaluru',
  state: 'Karnataka',
  postalCode: '560001',
  country: 'India',
});

const makeOrder = (): OrderDto => ({
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
  shippingAddress: makeAddress(),
  items: [],
  createdAt: '2026-08-18T11:00:00Z',
  updatedAt: '2026-08-18T11:00:00Z',
});

const makePaymentCheckout = (): PaymentCheckoutDto => ({
  paymentId: 'pay-1',
  paymentReference: 'PAY-ORD-2026-000001',
  attemptId: 'att-1',
  attemptReference: 'PAY-ORD-2026-000001-ATT-1',
  orderId: 'order-1',
  amount: 567.82,
  currency: 'INR',
  provider: 'MOCK',
  providerOrderId: 'order_mock_123',
  keyId: 'mock_key',
});

const makePayment = (): PaymentDto => ({
  id: 'pay-1',
  paymentReference: 'PAY-ORD-2026-000001',
  orderId: 'order-1',
  customerId: 'cust-1',
  amount: 567.82,
  currency: 'INR',
  status: 'SUCCESS',
  provider: 'MOCK',
  attempts: [],
  createdAt: '2026-08-18T11:00:00Z',
  updatedAt: '2026-08-18T11:00:00Z',
});

const makeReservation = (): ReservationDto => ({
  id: 'res-1',
  reservationReference: 'RSV-2026-000001',
  orderId: 'order-1',
  status: 'ACTIVE',
  expiresAt: '2026-08-18T11:15:00Z',
  releaseReason: null,
  items: [],
  createdAt: '2026-08-18T11:00:00Z',
  updatedAt: '2026-08-18T11:00:00Z',
});

const makeWrapper = (queryClient: QueryClient) => {
  const Wrapper: FC<{ children: ReactNode }> = ({ children }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/checkout']}>
        <Routes>
          <Route path="/checkout" element={<>{children}</>} />
          <Route
            path="/checkout/confirmation"
            element={<div data-testid="confirmation-route">Confirmation</div>}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
  return Wrapper;
};

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('usePlaceOrder (FD-12)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('runs the full backend pipeline: order → reserve → initiate → verify', async () => {
    vi.mocked(orderApi.createOrder).mockResolvedValue({ success: true, data: makeOrder() });
    vi.mocked(inventoryApi.reserveInventory).mockResolvedValue({ success: true, data: makeReservation() });
    vi.mocked(paymentApi.initiatePayment).mockResolvedValue({ success: true, data: makePaymentCheckout() });
    vi.mocked(paymentApi.verifyPayment).mockResolvedValue({ success: true, data: makePayment() });

    const queryClient = newClient();
    const Probe = () => {
      const placeOrder = usePlaceOrder();
      return (
        <div>
          <span data-testid="status">{placeOrder.isSuccess ? 'success' : 'idle'}</span>
          <button onClick={() => placeOrder.mutate({ shippingAddress: makeAddress() })}>place</button>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    fireEvent.click(screen.getByRole('button', { name: 'place' }));

    expect(await screen.findByTestId('confirmation-route')).toBeInTheDocument();

    const orderCall = vi.mocked(orderApi.createOrder).mock.calls[0][0];
    expect(orderCall.shippingAddress.postalCode).toBe('560001');
    expect(orderCall.idempotencyKey).toEqual(expect.any(String));
    expect(orderCall.customerNotes).toBeUndefined();

    expect(inventoryApi.reserveInventory).toHaveBeenCalledWith('order-1');
    expect(paymentApi.initiatePayment).toHaveBeenCalledWith('order-1');
    expect(paymentApi.verifyPayment).toHaveBeenCalledWith(
      expect.objectContaining({
        paymentReference: 'PAY-ORD-2026-000001',
        providerOrderId: 'order_mock_123',
        providerPaymentId: 'pay_order_mock_123',
        providerSignature: 'mock_provider_signature',
      })
    );
  });

  it('reuses a single idempotency key across retries within a session', async () => {
    vi.mocked(orderApi.createOrder).mockResolvedValue({ success: true, data: makeOrder() });
    vi.mocked(inventoryApi.reserveInventory).mockResolvedValue({ success: true, data: makeReservation() });
    vi.mocked(paymentApi.initiatePayment)
      .mockRejectedValueOnce(new ApiError('Payment unavailable', 'PAYMENT_PROVIDER_UNAVAILABLE', 503))
      .mockResolvedValueOnce({ success: true, data: makePaymentCheckout() });
    vi.mocked(paymentApi.verifyPayment).mockResolvedValue({ success: true, data: makePayment() });

    const queryClient = newClient();
    const Probe = () => {
      const placeOrder = usePlaceOrder();
      return (
        <div>
          <span data-testid="status">{placeOrder.isSuccess ? 'success' : 'idle'}</span>
          <button onClick={() => placeOrder.mutate({ shippingAddress: makeAddress() })}>place</button>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    const button = screen.getByRole('button', { name: 'place' });
    fireEvent.click(button);
    await waitFor(() => expect(orderApi.createOrder).toHaveBeenCalledTimes(1));
    fireEvent.click(button);
    expect(await screen.findByTestId('confirmation-route')).toBeInTheDocument();

    const keys = vi.mocked(orderApi.createOrder).mock.calls.map((call) => call[0].idempotencyKey);
    expect(keys).toHaveLength(2);
    expect(keys[0]).toBe(keys[1]);
  });

  it('releases the reservation best-effort when payment fails', async () => {
    vi.mocked(orderApi.createOrder).mockResolvedValue({ success: true, data: makeOrder() });
    vi.mocked(inventoryApi.reserveInventory).mockResolvedValue({ success: true, data: makeReservation() });
    vi.mocked(paymentApi.initiatePayment).mockRejectedValue(
      new ApiError('Payment could not be verified.', 'PAYMENT_VERIFICATION_FAILED', 400)
    );

    const queryClient = newClient();
    const Probe = () => {
      const placeOrder = usePlaceOrder();
      return (
        <div>
          <span data-testid="status">{placeOrder.isError ? 'error' : 'idle'}</span>
          <button onClick={() => placeOrder.mutate({ shippingAddress: makeAddress() })}>place</button>
        </div>
      );
    };

    render(<Probe />, { wrapper: makeWrapper(queryClient) });
    fireEvent.click(screen.getByRole('button', { name: 'place' }));

    await waitFor(() => expect(screen.getByTestId('status')).toHaveTextContent('error'));
    expect(inventoryApi.releaseReservation).toHaveBeenCalledWith('res-1', 'PAYMENT_FAILED');
    expect(screen.queryByTestId('confirmation-route')).not.toBeInTheDocument();
  });
});