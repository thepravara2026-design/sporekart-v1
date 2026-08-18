import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductListPage } from '../pages/ProductListPage';
import { ProductDetailPage } from '../pages/ProductDetailPage';
import { catalogApi } from '../../../services/catalogApi';
import { cartApi } from '../../../services/cartApi';
import { ApiError } from '../../../services/apiError';
import { Product, Category, PageResponse } from '../../../types/catalog';
import { ToastProvider } from '../../../components/ui/Toast';

vi.mock('../../../services/catalogApi');
vi.mock('../../../services/cartApi', () => ({
  cartApi: { addItem: vi.fn() },
}));

const mockCategory: Category = {
  id: 'cat-1111',
  name: 'Liquid Cultures',
  slug: 'liquid-cultures',
  description: 'Fresh liquid cultures for gourmet and medicinal mushrooms.',
  status: 'ACTIVE',
  createdAt: '2026-08-14T10:00:00Z',
  updatedAt: '2026-08-14T10:00:00Z',
};

const mockProduct1: Product = {
  id: 'prod-1111',
  sku: 'SP-LC-001',
  name: 'Blue Oyster Culture',
  description: 'High-yield gourmet culture',
  price: 249.0,
  currency: 'INR',
  status: 'ACTIVE',
  category: mockCategory,
  createdAt: '2026-08-14T10:00:00Z',
  updatedAt: '2026-08-14T10:00:00Z',
};

const mockProductsPage: PageResponse<Product> = {
  content: [mockProduct1],
  page: 0,
  size: 12,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
};

const mockCategoriesPage: PageResponse<Category> = {
  content: [mockCategory],
  page: 0,
  size: 100,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
};

const cartResponse = {
  success: true,
  data: {
    id: 'cart-1',
    customerId: 'cust-1',
    status: 'ACTIVE',
    currency: 'USD',
    items: [
      {
        id: 'item-1',
        productId: 'prod-1111',
        productName: 'Blue Oyster Culture',
        sku: 'SP-LC-001',
        variantId: null,
        variantName: null,
        unitPrice: 249.0,
        quantity: 2,
        lineTotal: 498.0,
        createdAt: '',
        updatedAt: '',
      },
    ],
    itemCount: 2,
    subtotal: 498.0,
    createdAt: '',
    updatedAt: '',
  },
};

function createQueryClient() {
  return new QueryClient({ defaultOptions: { queries: { retry: false } } });
}

function Wrapper({ children, route = '/products' }: { children: React.ReactNode; route?: string }) {
  return (
    <QueryClientProvider client={createQueryClient()}>
      <ToastProvider>
        <MemoryRouter initialEntries={[route]}>
          {children}
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

const fullRoutes = (
  <Routes>
    <Route path="/products" element={<ProductListPage />} />
    <Route path="/products/:productId" element={<ProductDetailPage />} />
  </Routes>
);

describe('Product Purchase Integration Flow (FD-10)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(catalogApi.getCategories).mockResolvedValue({ success: true, data: mockCategoriesPage });
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: mockProductsPage });
  });

  it('navigates catalog → product detail → quantity → add to cart → cart sync', async () => {
    vi.mocked(catalogApi.getProduct).mockResolvedValue({ success: true, data: mockProduct1 });
    vi.mocked(cartApi.addItem).mockResolvedValue(cartResponse);

    const queryClient = createQueryClient();
    render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <MemoryRouter initialEntries={['/products']}>{fullRoutes}</MemoryRouter>
        </ToastProvider>
      </QueryClientProvider>
    );

    // 1. Catalog listing renders.
    await screen.findByTestId('products-grid');
    expect(screen.getByText('Blue Oyster Culture')).toBeInTheDocument();

    // 2. Navigate to the product detail page.
    fireEvent.click(screen.getByRole('link', { name: /view details/i }));
    await screen.findByTestId('product-detail-card');
    expect(screen.getByText('SKU: SP-LC-001')).toBeInTheDocument();
    expect(screen.getByText(/249/)).toBeInTheDocument();
    expect(screen.getAllByText('In Stock').length).toBeGreaterThan(0);

    // 3. Select quantity.
    const quantityInput = screen.getByRole('spinbutton', { name: 'Quantity' });
    expect(quantityInput).toHaveValue(1);
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity' }));
    expect(quantityInput).toHaveValue(2);

    // 4. Add to cart with the pending loading state and successful API response.
    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));
    await waitFor(() => {
      expect(cartApi.addItem).toHaveBeenCalledWith({ productId: 'prod-1111', quantity: 2 });
    });

    // 5. Success feedback via toast.
    expect(await screen.findByText('Added to Cart')).toBeInTheDocument();

    // 6. Cart state (cache) synchronized for the future cart page.
    await waitFor(() => {
      expect(queryClient.getQueryData(['cart'])).toEqual(cartResponse);
    });

    // 7. No error region on success.
    expect(screen.queryByTestId('add-to-cart-error')).not.toBeInTheDocument();
  });

  it('blocks purchase of out-of-stock products', async () => {
    const outOfStock: Product = { ...mockProduct1, status: 'OUT_OF_STOCK' };
    vi.mocked(catalogApi.getProduct).mockResolvedValue({ success: true, data: outOfStock });

    render(
      <Wrapper route="/products/prod-1111">{fullRoutes}</Wrapper>
    );

    await screen.findByTestId('product-detail-card');
    expect(screen.getByRole('button', { name: 'Out of Stock' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Out of Stock' }));
    expect(cartApi.addItem).not.toHaveBeenCalled();
  });

  it('survives an image failure without breaking the purchase experience', async () => {
    vi.mocked(catalogApi.getProduct).mockResolvedValue({ success: true, data: mockProduct1 });

    render(
      <Wrapper route="/products/prod-1111">{fullRoutes}</Wrapper>
    );

    await screen.findByTestId('product-detail-card');
    // No image URLs are supplied by the catalog contract, so the placeholder
    // is rendered; the Add to Cart action must remain intact.
    expect(screen.getByText('Mushroom Spawn')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add to Cart' })).toBeEnabled();
  });

  it('renders the not-found experience for an invalid product route', async () => {
    vi.mocked(catalogApi.getProduct).mockRejectedValue(
      new ApiError('Product not found', 'CATALOG_PRODUCT_NOT_FOUND', 404)
    );

    render(
      <Wrapper route="/products/does-not-exist">{fullRoutes}</Wrapper>
    );

    expect(await screen.findByTestId('product-not-found')).toBeInTheDocument();
    expect(cartApi.addItem).not.toHaveBeenCalled();
  });
});