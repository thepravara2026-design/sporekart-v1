import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CheckoutPage } from '../CheckoutPage';
import { cartApi, CheckoutPreviewResponse } from '../../../../services/cartApi';
import { orderApi, OrderDto } from '../../../../services/orderApi';
import { paymentApi, PaymentCheckoutDto, PaymentDto } from '../../../../services/paymentApi';
import { ToastProvider } from '../../../../components/ui/Toast';
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

const makePreview = (): CheckoutPreviewResponse => ({
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

const renderCheckoutPage = (queryClient: QueryClient, route = '/checkout') => {
  return render(
    <QueryClientProvider client={queryClient}>
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

describe('CheckoutPage (FD-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
  });

  it('requires sign-in before showing the checkout flow', async () => {
    localStorage.removeItem('accessToken');
    renderCheckoutPage(newClient());
    expect(screen.getByText('Sign in required')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toBeInTheDocument();
  });

  it('shows the empty cart state when the cart is empty', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 0, items: [], subtotal: 0 }));
    renderCheckoutPage(newClient());
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
  });

  it('runs the full multi-step flow: shipping → preview → place order → confirmation', async () => {
    const queryClient = newClient();
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 2 }));
    vi.mocked(cartApi.generateCheckoutPreview).mockResolvedValue({ success: true, data: makePreview() });
    vi.mocked(orderApi.createOrder).mockResolvedValue({ success: true, data: makeOrder() });
    vi.mocked(paymentApi.initiatePayment).mockResolvedValue({ success: true, data: makePaymentCheckout() });
    vi.mocked(paymentApi.verifyPayment).mockResolvedValue({ success: true, data: makePayment() });

    renderCheckoutPage(queryClient);

    // Step 0 — Shipping.
    await screen.findByTestId('checkout-shipping-form');
    expect(screen.getByTestId('checkout-stepper')).toBeInTheDocument();
    fillShippingForm();
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Payment' }));

    // Step 1 — Payment: preview fetched, summary rendered.
    await screen.findByTestId('checkout-payment-form');
    await waitFor(() => {
      expect(cartApi.generateCheckoutPreview).toHaveBeenCalledWith({
        destinationAddress: '42 Fungal Lane, Bengaluru, Karnataka, 560001',
      });
    });
    await screen.findByTestId('checkout-summary-total');
    expect(screen.getByTestId('checkout-summary-total')).toHaveTextContent('₹567.82');

    // Place order → backend pipeline → confirmation route.
    fireEvent.click(screen.getByRole('button', { name: 'Place Order & Pay' }));

    await waitFor(() => {
      expect(orderApi.createOrder).toHaveBeenCalled();
    });
    expect(paymentApi.initiatePayment).toHaveBeenCalledWith('order-1');
    expect(paymentApi.verifyPayment).toHaveBeenCalledWith(
      expect.objectContaining({ paymentReference: 'PAY-ORD-2026-000001', providerOrderId: 'order_mock_123' })
    );
    expect(await screen.findByTestId('confirmation-route')).toBeInTheDocument();
  });

  it('surfaces preview warnings from the backend before placement', async () => {
    vi.mocked(cartApi.getCart).mockResolvedValue(makeCartResponse({ itemCount: 2 }));
    vi.mocked(cartApi.generateCheckoutPreview).mockResolvedValue({
      success: true,
      data: makePreview(),
    });
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
    fireEvent.click(screen.getByRole('button', { name: 'Continue to Payment' }));

    expect(await screen.findByText('The price of this item changed since you added it.')).toBeInTheDocument();
  });
});