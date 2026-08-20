import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductActions } from '../ProductActions';
import { Product } from '../../types/catalog';
import { cartApi } from '../../../../services/cartApi';
import { ApiError } from '../../../../services/apiError';
import { ToastProvider } from '../../../../components/ui/Toast';
import { AuthProvider } from '../../../../context/AuthContext';

vi.mock('../../../../services/cartApi', () => ({
  cartApi: { addItem: vi.fn() },
}));

const baseProduct: Product = {
  id: 'prod-707',
  sku: 'SPW-LION-001',
  name: 'Lions Mane Liquid Culture',
  description: 'Pure liquid culture syringe for lions mane cultivation.',
  price: 28.0,
  currency: 'INR',
  status: 'ACTIVE',
  category: {
    id: 'cat-70',
    name: 'Medicinal Cultures',
    slug: 'medicinal-cultures',
    description: null,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

const renderActions = (product: Product) => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const view = render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AuthProvider>
          <ToastProvider>
            <ProductActions product={product} />
          </ToastProvider>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
  return { queryClient, view };
};

describe('ProductActions — Add to Cart (FD-10)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('token', 'mock-jwt-customer-token');
    localStorage.setItem('sporekart_user', JSON.stringify({
      id: 'usr-customer-01',
      name: 'Mushroom Cultivator',
      email: 'customer@sporekart.com',
      role: 'ROLE_CUSTOMER',
      roles: ['ROLE_CUSTOMER'],
    }));
  });

  it('renders an enabled Add to Cart action for a purchasable product', () => {
    renderActions(baseProduct);
    const btn = screen.getByRole('button', { name: 'Add to Cart' });
    expect(btn).toBeEnabled();
  });

  it('submits the product id and selected quantity to the cart API', async () => {
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockResolvedValue({
      success: true,
      data: { id: 'cart-1', customerId: 'cust-1', items: [], totalQuantity: 1, subtotal: 28.0, status: 'ACTIVE', createdAt: '', updatedAt: '' },
    });
    const { view } = renderActions(baseProduct);

    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity' }));
    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));

    await waitFor(() => {
      expect(cartApi.addItem).toHaveBeenCalledWith({ productId: 'prod-707', quantity: 2 });
    });
    expect(view.getByText('Added to Cart')).toBeInTheDocument();
  });

  it('prevents duplicate submissions while a request is pending', async () => {
    let resolveAdd: (value: unknown) => void = () => {};
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise((resolve) => { resolveAdd = resolve; })
    );
    renderActions(baseProduct);

    const btn = screen.getByRole('button', { name: 'Add to Cart' });
    fireEvent.click(btn);
    await waitFor(() => {
      expect(cartApi.addItem).toHaveBeenCalledTimes(1);
    });
    // Button is disabled and busy while pending — a second click cannot submit.
    expect(btn).toBeDisabled();
    expect(btn).toHaveAttribute('aria-busy', 'true');
    fireEvent.click(btn);
    expect(cartApi.addItem).toHaveBeenCalledTimes(1);

    resolveAdd({ success: true, data: { id: 'cart-1', customerId: 'c', items: [], totalQuantity: 1, subtotal: 1, status: 'ACTIVE', createdAt: '', updatedAt: '' } });
  });

  it('shows an inline error and preserves user input when the API rejects', async () => {
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockRejectedValue(
      new ApiError('Inventory unavailable for this product.', 'CART_INVALID_QUANTITY', 400)
    );
    const { view } = renderActions(baseProduct);

    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity' }));
    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));

    await waitFor(() => {
      expect(view.getByTestId('add-to-cart-error')).toBeInTheDocument();
    });
    expect(view.getByTestId('add-to-cart-error')).toHaveTextContent('Inventory unavailable for this product.');
    // Quantity selection is preserved for retry.
    expect(screen.getByRole('spinbutton', { name: 'Quantity' })).toHaveValue(2);
  });

  it('surfaces an unauthorized response without a success claim', async () => {
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockRejectedValue(
      new ApiError('Authentication required.', 'UNAUTHORIZED', 401)
    );
    const { view } = renderActions(baseProduct);

    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));

    await waitFor(() => {
      expect(view.getByTestId('add-to-cart-error')).toBeInTheDocument();
    });
    expect(view.getByTestId('add-to-cart-error')).toHaveTextContent('Authentication required.');
    expect(screen.queryByText('Added to Cart')).not.toBeInTheDocument();
  });

  it('disables purchase controls and never calls the API for out-of-stock products', () => {
    const outOfStock: Product = { ...baseProduct, status: 'OUT_OF_STOCK' };
    renderActions(outOfStock);

    expect(screen.getByRole('button', { name: 'Out of Stock' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Decrease quantity' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeDisabled();

    fireEvent.click(screen.getByRole('button', { name: 'Out of Stock' }));
    expect(cartApi.addItem).not.toHaveBeenCalled();
  });

  it('treats draft products as non-purchasable with an honest label', () => {
    const draft: Product = { ...baseProduct, status: 'DRAFT' };
    renderActions(draft);
    expect(screen.getByRole('button', { name: 'Draft' })).toBeDisabled();
    expect(cartApi.addItem).not.toHaveBeenCalled();
  });

  it('disables the add-to-cart action for discontinued products', () => {
    const discontinued: Product = { ...baseProduct, status: 'DISCONTINUED' };
    renderActions(discontinued);
    expect(screen.getByRole('button', { name: 'Discontinued' })).toBeDisabled();
  });
});
