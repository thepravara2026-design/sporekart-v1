import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductDetailPage } from '../ProductDetailPage';
import { catalogApi } from '../../../../services/catalogApi';
import { cartApi } from '../../../../services/cartApi';
import { Product } from '../../../../types/catalog';
import { ApiError } from '../../../../services/apiError';
import { ToastProvider } from '../../../../components/ui/Toast';
import { AuthProvider } from '../../../../context/AuthContext';

vi.mock('../../../../services/catalogApi', () => ({
  catalogApi: {
    getProduct: vi.fn(),
    getProducts: vi.fn(),
  },
}));

vi.mock('../../../../services/cartApi', () => ({
  cartApi: { addItem: vi.fn() },
}));

const mockProduct: Product = {
  id: 'prod-99',
  sku: 'SKU-TURKEY-001',
  name: 'Turkey Tail Extract',
  description: 'Immune support extract',
  price: 29.99,
  currency: 'INR',
  status: 'ACTIVE',
  category: {
    id: 'cat-1',
    name: 'Medicinal',
    slug: 'medicinal',
    description: null,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

const renderPage = (productId = 'prod-99') => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const ui = (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <MemoryRouter initialEntries={[`/products/${productId}`]}>
            <Routes>
              <Route path="/products/:productId" element={<ProductDetailPage />} />
            </Routes>
          </MemoryRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
  const view = render(ui);
  return { queryClient, view };
};

describe('ProductDetailPage (FD-10)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('token', 'jwt-token');
    localStorage.setItem('sporekart_user', JSON.stringify({
      id: 'usr-customer-01', name: 'Test User', email: 'customer@sporekart.com',
      role: 'ROLE_CUSTOMER', roles: ['ROLE_CUSTOMER'],
    }));
  });

  it('renders the loading skeleton while the product query is pending', () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));
    renderPage();
    expect(screen.getByTestId('product-detail-loading')).toBeInTheDocument();
  });

  it('renders full product details when the query succeeds', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });
    renderPage();

    expect(await screen.findByTestId('product-detail-card')).toBeInTheDocument();
    expect(screen.getAllByText('Turkey Tail Extract').length).toBeGreaterThan(0);
    expect(screen.getByText('SKU: SKU-TURKEY-001')).toBeInTheDocument();
    expect(screen.getByText(/₹\s*29\.99/)).toBeInTheDocument();
    expect(screen.getByText('Immune support extract')).toBeInTheDocument();
    expect(screen.getAllByText('In Stock').length).toBeGreaterThan(0);
  });

  it('renders exactly one H1 for the product', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });
    renderPage();
    await screen.findByTestId('product-detail-card');
    const headings = screen.getAllByRole('heading', { level: 1 });
    expect(headings).toHaveLength(1);
    expect(headings[0]).toHaveTextContent('Turkey Tail Extract');
  });

  it('renders breadcrumb trail with Home, Products and category', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });
    renderPage();
    await screen.findByTestId('product-detail-card');
    expect(screen.getByRole('link', { name: 'Home' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Products' })).toBeInTheDocument();
    // Category appears in both the breadcrumb and the product information panel.
    expect(screen.getAllByRole('link', { name: 'Medicinal' }).length).toBeGreaterThanOrEqual(2);
  });

  it('renders the branded product image placeholder from the gallery', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });
    renderPage();
    await screen.findByTestId('product-detail-card');
    expect(screen.getByText('Mushroom Spawn')).toBeInTheDocument();
  });

  it('renders the not-found experience for a missing product', async () => {
    const error404 = new ApiError('Product not found', 'CATALOG_PRODUCT_NOT_FOUND', 404);
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockRejectedValueOnce(error404);
    renderPage('invalid-id');

    expect(await screen.findByTestId('product-not-found')).toBeInTheDocument();
    expect(screen.getByText('Product Not Found')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Browse All Products' })).toHaveAttribute('href', '/products');
  });

  it('renders a retryable error state for API failures', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockRejectedValueOnce(
      new Error('Network error: Backend unavailable')
    );
    renderPage();

    expect(await screen.findByTestId('product-detail-error')).toBeInTheDocument();
    expect(screen.getByText(/network error/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();
  });

  it('recovers after retry when the API succeeds on the second attempt', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>)
      .mockRejectedValueOnce(new Error('Network error: Backend unavailable'))
      .mockResolvedValueOnce({ success: true, data: mockProduct });
    renderPage();

    expect(await screen.findByTestId('product-detail-error')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));
    expect(await screen.findByTestId('product-detail-card')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-TURKEY-001')).toBeInTheDocument();
  });

  it('disables the purchase action for out-of-stock products', async () => {
    const outOfStock: Product = { ...mockProduct, status: 'OUT_OF_STOCK' };
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: outOfStock,
    });
    renderPage();

    await screen.findByTestId('product-detail-card');
    expect(screen.getAllByText('Out of Stock').length).toBeGreaterThan(0);
    expect(screen.getByRole('button', { name: 'Out of Stock' })).toBeDisabled();
  });

  it('adds a selected quantity to the cart and syncs the cart cache', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });
    const cartResponse = {
      success: true,
      data: {
        id: 'cart-1',
        customerId: 'cust-1',
        status: 'ACTIVE',
        currency: 'INR',
        items: [{
          id: 'item-1',
          productId: 'prod-99',
          productName: 'Turkey Tail Extract',
          sku: 'SKU-TURKEY-001',
          variantId: null,
          variantName: null,
          unitPrice: 29.99,
          quantity: 3,
          lineTotal: 89.97,
          createdAt: '2026-08-18T00:00:00Z',
          updatedAt: '2026-08-18T00:00:00Z',
        }],
        itemCount: 3,
        subtotal: 89.97,
        createdAt: '2026-08-18T00:00:00Z',
        updatedAt: '2026-08-18T00:00:00Z',
      },
    };
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(cartResponse);
    const { queryClient } = renderPage();

    await screen.findByTestId('product-detail-card');
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity' }));
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity' }));
    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));

    await waitFor(() => {
      expect(cartApi.addItem).toHaveBeenCalledWith({ productId: 'prod-99', quantity: 3 });
    });
    expect(await screen.findByText('Added to Cart')).toBeInTheDocument();
    expect(queryClient.getQueryData(['cart'])).toEqual(cartResponse);
  });

  it('does not claim success when the cart API rejects', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockRejectedValueOnce(
      new ApiError('Unable to reserve requested inventory.', 'CART_INVALID_QUANTITY', 400)
    );
    renderPage();

    await screen.findByTestId('product-detail-card');
    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));

    expect(await screen.findByTestId('add-to-cart-error')).toBeInTheDocument();
    expect(screen.queryByText('Added to Cart')).not.toBeInTheDocument();
    expect(screen.getByTestId('add-to-cart-error')).toHaveTextContent('Unable to reserve requested inventory.');
  });
});
