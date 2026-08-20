import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CheckoutPage } from '../CheckoutPage';
import { cartApi, CheckoutPreviewResponse } from '../../../../services/cartApi';
import { orderApi, OrderDto } from '../../../../services/orderApi';
import { paymentApi, PaymentCheckoutDto, PaymentDto } from '../../../../services/paymentApi';
import { inventoryApi, ReservationDto } from '../../../../services/inventoryApi';
import { authApi, UserProfileDto } from '../../../../services/authApi';
import { ToastProvider } from '../../../../components/ui/Toast';
import { AuthProvider } from '../../../../context/AuthContext';
import { makeCartResponse } from '../../../cart/__tests__/fixtures';

vi.mock('../../../../services/cartApi', () => ({
  cartApi: {
    getCart: vi.fn(),
    addItem: vi.fn(),
    updateItemQuantity: vi.fn(),
    removeItem: vi.fn(),
    clearCart: vi.fn(),
    generateCheckoutPreview: vi.fn(),
  },
}));

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
    listAdminInventory: vi.fn(),
    getAdminInventoryBySku: vi.fn(),
    listMovements: vi.fn(),
    adjustStock: vi.fn(),
    recordDamagedStock: vi.fn(),
  },
}));

vi.mock('../../../../services/authApi', () => ({
  authApi: {
    getCurrentUser: vi.fn(),
    login: vi.fn(),
    register: vi.fn(),
    refreshToken: vi.fn(),
    changePassword: vi.fn(),
    getSessions: vi.fn(),
  },
}));

const makePreview = (overrides: Partial<CheckoutPreviewResponse> = {}): CheckoutPreviewResponse => ({
  previewId: 'preview-1',
  cartId: 'cart-1',
  customerId: 'cust-1',
  currency: 'INR',
  items: [
    {
      cartItemId: 'item-1',
      productId: 'prod-1',
      sku: 'SKU-OYSTER-01',
      productName: 'Blue Oyster Spawn',
      quantity: 2,
      cartUnitPrice: 249,
      authoritativeUnitPrice: 249,
      priceChanged: false,
      lineSubtotal: 498,
      discountAmount: 0,
      taxAmount: 44.82,
      lineTotal: 542.82,
    },
  ],
  breakdown: { subtotal: 498, discountTotal: 0, taxTotal: 44.82, shippingFee: 25, grandTotal: 567.82, currency: 'INR' },
  warnings: [],
  generatedAt: '2026-08-18T11:00:00Z',
  ...overrides,
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

const makeProfile = (): UserProfileDto => ({
  id: 'cust-1',
  email: 'buyer@example.com',
  firstName: 'A.',
  lastName: 'Buyer',
  role: 'CUSTOMER',
  status: 'ACTIVE',
});

const renderCheckoutPage = (queryClient: QueryClient, route = '/checkout') => {
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <MemoryRouter initialEntries={[route]}>
            <Routes>
              <Route path="/checkout" element={<CheckoutPage />} />
              <Route
                path="/checkout/confirmation"
                element={<div data-testid="confirmation-route">Confirmation</div>}
              />
            </Routes>
          </MemoryRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

const fillShippingForm = () => {
  fireEvent.change(screen.getByLabelText(/Full name/), { target: { value: 'A. Buyer' } });
  fireEvent.change(screen.getByLabelText(/Phone/), { target: { value: '+919876543210' } });
  fireEvent.change(screen.getByLabelText(/Address line 1/), { target: { value: '42 Fungal Lane' } });
  fireEvent.change(screen.getByLabelText(/City/), { target: { value: 'Bengaluru' } });
  fireEvent.change(screen.getByLabelText(/State/), { target: { value: 'Karnataka' } });
  fireEvent.change(screen.getByLabelText(/Postal code/), { target: { value: '560001' } });
};

describe('CheckoutPage (FD-12)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
    localStorage.setItem('token', 'jwt-token');
    localStorage.setItem('sporekart_user', JSON.stringify({ id: 'cust-1', name: 'A. Buyer', email: 'buyer@example.com', role: 'ROLE_CUSTOMER', roles: ['ROLE_CUSTOMER'] }));
  });

  it('requires sign-in before showing the checkout flow', async () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('token');
    localStorage.removeItem('sporekart_user');
    renderCheckoutPage(newClient());
    expect(screen.queryByTestId('checkout-shipping-form')).not.toBeInTheDocument();
    expect(screen.queryByTestId('checkout-stepper')).not.toBeInTheDocument();
  });

  it('shows the empty cart state when the cart is empty', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 0, items: [], subtotal: 0 }));
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(makeProfile());
    renderCheckoutPage(newClient());
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
  });

  it('prefills customer information from the profile', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 2 }));
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(makeProfile());

    renderCheckoutPage(newClient());

    await screen.findByTestId('checkout-shipping-form');
    expect(screen.getByText('A. Buyer')).toBeInTheDocument();
    expect(screen.getByLabelText(/Full name/)).toHaveValue('A. Buyer');
  });

  it('runs the 3-step flow: delivery → review → payment → confirmation', async () => {
    const queryClient = newClient();
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 2 }));
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(makeProfile());
    vi.mocked(cartApi.generateCheckoutPreview).mockResolvedValue({ success: true, data: makePreview() });
    vi.mocked(orderApi.createOrder).mockResolvedValue({ success: true, data: makeOrder() });
    vi.mocked(inventoryApi.reserveInventory).mockResolvedValue({ success: true, data: makeReservation() });
    vi.mocked(paymentApi.initiatePayment).mockResolvedValue({ success: true, data: makePaymentCheckout() });
    vi.mocked(paymentApi.verifyPayment).mockResolvedValue({ success: true, data: makePayment() });

    renderCheckoutPage(queryClient);

    // Step 0 — Customer & Delivery.
    await screen.findByTestId('checkout-shipping-form');
    expect(screen.getByTestId('checkout-stepper')).toBeInTheDocument();
    expect(screen.getByTestId('checkout-step-0')).toHaveAttribute('aria-current', 'step');
    fillShippingForm();
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Review' }));

    // Step 1 — Review & Confirm: preview fetched, totals rendered.
    await screen.findByTestId('checkout-order-review');
    await waitFor(() => {
      expect(cartApi.generateCheckoutPreview).toHaveBeenCalledWith({
        destinationAddress: '42 Fungal Lane, Bengaluru, Karnataka, 560001',
      });
    });
    await screen.findByTestId('checkout-summary-total');
    expect(screen.getByTestId('checkout-summary-total')).toHaveTextContent('₹567.82');
    expect(screen.getByTestId('checkout-step-1')).toHaveAttribute('aria-current', 'step');
    expect(screen.getByText('A. Buyer')).toBeInTheDocument();
    expect(screen.getByText('42 Fungal Lane')).toBeInTheDocument();

    // Continue to the payment step.
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Payment' }));
    await screen.findByTestId('checkout-payment-form');
    expect(screen.getByTestId('checkout-step-2')).toHaveAttribute('aria-current', 'step');

    // Step 2 — Payment: place order → backend pipeline → confirmation route.
    fireEvent.click(screen.getByRole('button', { name: 'Place Order & Pay' }));

    await waitFor(() => {
      expect(orderApi.createOrder).toHaveBeenCalledTimes(1);
    });
    expect(orderApi.createOrder).toHaveBeenCalledWith(
      expect.objectContaining({ shippingAddress: expect.objectContaining({ postalCode: '560001' }) })
    );
    expect(inventoryApi.reserveInventory).toHaveBeenCalledWith('order-1');
    expect(paymentApi.initiatePayment).toHaveBeenCalledWith('order-1');
    expect(paymentApi.verifyPayment).toHaveBeenCalledWith(
      expect.objectContaining({ paymentReference: 'PAY-ORD-2026-000001', providerOrderId: 'order_mock_123' })
    );
    expect(await screen.findByTestId('confirmation-route')).toBeInTheDocument();
  });

  it('surfaces preview warnings from the backend before placement', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 2 }));
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(makeProfile());
    vi.mocked(cartApi.generateCheckoutPreview).mockImplementation(async () => ({
      success: true,
      data: {
        ...makePreview(),
        warnings: [
          { type: 'PRICE_CHANGED', productId: 'prod-1', message: 'The price of this item changed since you added it.' },
        ],
      },
    }));

    renderCheckoutPage(newClient());
    await screen.findByTestId('checkout-shipping-form');
    fillShippingForm();
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Review' }));

    expect(await screen.findByText('The price of this item changed since you added it.')).toBeInTheDocument();
  });

  it('blocks placement and prompts review when totals change on revalidation', async () => {
    const queryClient = newClient();
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 2 }));
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(makeProfile());

    let calls = 0;
    vi.mocked(cartApi.generateCheckoutPreview).mockImplementation(async () => {
      calls += 1;
      const base = makePreview();
      if (calls === 1) return { success: true, data: base };
      return {
        success: true,
        data: { ...base, breakdown: { ...base.breakdown, grandTotal: 612.82 } },
      };
    });

    renderCheckoutPage(queryClient);
    await screen.findByTestId('checkout-shipping-form');
    fillShippingForm();
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Review' }));
    await screen.findByTestId('checkout-order-review');

    fireEvent.click(screen.getByRole('button', { name: 'Continue to Payment' }));
    await screen.findByTestId('checkout-payment-form');

    fireEvent.click(screen.getByRole('button', { name: 'Place Order & Pay' }));

    expect(await screen.findByText('The order total has changed. Please review your order.')).toBeInTheDocument();
    expect(orderApi.createOrder).not.toHaveBeenCalled();
  });
});
