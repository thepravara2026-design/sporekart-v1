import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MainLayout } from '../../../layouts/MainLayout';
import { ProductListPage } from '../../catalog/pages/ProductListPage';
import { ProductDetailPage } from '../../catalog/pages/ProductDetailPage';
import { CartPage } from '../pages/CartPage';
import { AuthProvider } from '../../../context/AuthContext';
import { catalogApi } from '../../../services/catalogApi';
import { cartApi } from '../../../services/cartApi';
import { ApiError } from '../../../services/apiError';
import { ToastProvider } from '../../../components/ui/Toast';
import { Product, Category, PageResponse } from '../../../types/catalog';
import { makeCartResponse } from './fixtures';

vi.mock('../../../services/catalogApi', () => ({
  catalogApi: {
    getProducts: vi.fn(),
    getProduct: vi.fn(),
    getCategories: vi.fn(),
    getCategory: vi.fn(),
  },
}));

vi.mock('../../../services/cartApi', () => ({
  cartApi: {
    getCart: vi.fn(),
    addItem: vi.fn(),
    updateItemQuantity: vi.fn(),
    removeItem: vi.fn(),
    clearCart: vi.fn(),
  },
}));

const mockCategory: Category = {
  id: 'cat-1111',
  name: 'Liquid Cultures',
  slug: 'liquid-cultures',
  description: 'Fresh liquid cultures.',
  status: 'ACTIVE',
  createdAt: '2026-08-14T10:00:00Z',
  updatedAt: '2026-08-14T10:00:00Z',
};

const mockProduct: Product = {
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

const catalogPages = () => ({
  content: [mockProduct],
  page: 0,
  size: 12,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
});

const categoryPages = (): PageResponse<Category> => ({
  content: [mockCategory],
  page: 0,
  size: 100,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
});

const cartItemFixture = () => ({
  id: 'item-1',
  productId: 'prod-1111',
  variantId: null,
  sku: 'SP-LC-001',
  productName: 'Blue Oyster Culture',
  variantName: null,
  unitPrice: 249.0,
  quantity: 1,
  lineTotal: 249.0,
  createdAt: '',
  updatedAt: '',
});

const renderApp = (queryClient: QueryClient, route = '/products') => {
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <MemoryRouter initialEntries={[route]}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/products" element={<ProductListPage />} />
                <Route path="/products/:productId" element={<ProductDetailPage />} />
                <Route path="/cart" element={<CartPage />} />
              </Route>
            </Routes>
          </MemoryRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

const newClient = () =>
  new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('Cart Purchase Integration Flows (FD-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('accessToken', 'jwt-token');
    vi.mocked(catalogApi.getCategories).mockResolvedValue({ success: true, data: categoryPages() });
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: catalogPages() });
    vi.mocked(catalogApi.getProduct).mockResolvedValue({ success: true, data: mockProduct });
  });

  it('FLOW A — complete cart flow: catalog → detail → add → cart → quantity → remove → empty → continue shopping', async () => {
    const queryClient = newClient();
    let current = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
    vi.mocked(cartApi.getCart).mockImplementation(() => Promise.resolve(current));
    vi.mocked(cartApi.addItem).mockImplementation(async () => {
      current = makeCartResponse({
        itemCount: 1,
        subtotal: 249,
        items: [cartItemFixture()],
      });
      return current;
    });
    vi.mocked(cartApi.updateItemQuantity).mockImplementation(async (_itemId, { quantity }) => {
      current = makeCartResponse({
        itemCount: quantity,
        subtotal: 249 * quantity,
        items: [{ ...cartItemFixture(), quantity, lineTotal: 249 * quantity }],
      });
      return current;
    });
    vi.mocked(cartApi.removeItem).mockImplementation(async () => {
      current = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
      return current;
    });

    renderApp(queryClient);

    // 1. Catalog listing renders the product.
    await screen.findByTestId('products-grid');
    expect(screen.getByText('Blue Oyster Culture')).toBeInTheDocument();

    // 2. Navigate to the product detail page.
    fireEvent.click(screen.getByRole('link', { name: /view details/i }));
    await screen.findByTestId('product-detail-card');

    // 3. Add to cart; the header count synchronizes.
    fireEvent.click(screen.getByRole('button', { name: 'Add to Cart' }));
    expect(await screen.findByText('Added to Cart')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByTestId('cart-count-badge')).toHaveTextContent('1');
    });

    // 4. Open the cart drawer via the header cart trigger, then open the full cart page.
    fireEvent.click(screen.getByRole('button', { name: 'View Shopping Cart (1 item)' }));
    expect(await screen.findByTestId('cart-drawer')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('link', { name: 'View Cart' }));
    await screen.findByTestId('cart-layout');
    expect(screen.getByRole('link', { name: 'Blue Oyster Culture' })).toBeInTheDocument();
    expect(screen.getByText(/Line total: ₹\s*249\.00/)).toBeInTheDocument();

    // 5. Change quantity; line total, summary, and badge update.
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Culture' }));
    await waitFor(() => {
      expect(screen.getByText(/Line total: ₹\s*498\.00/)).toBeInTheDocument();
      expect(screen.getByTestId('cart-summary-total')).toHaveTextContent(/₹\s*498\.00/);
    });
    await waitFor(() => {
      expect(screen.getByTestId('cart-count-badge')).toHaveTextContent('2');
    });

    // 6. Remove the item → empty state and zero count.
    fireEvent.click(screen.getByRole('button', { name: 'Remove Blue Oyster Culture from cart' }));
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
    expect(screen.queryByTestId('cart-count-badge')).not.toBeInTheDocument();

    // 7. Continue shopping returns to the catalog.
    fireEvent.click(screen.getByRole('link', { name: 'Continue Shopping' }));
    expect(await screen.findByTestId('products-grid')).toBeInTheDocument();

    expect(cartApi.addItem).toHaveBeenCalledWith({ productId: 'prod-1111', quantity: 1 });
    expect(cartApi.updateItemQuantity).toHaveBeenCalledWith('item-1', { quantity: 2 });
    expect(cartApi.removeItem).toHaveBeenCalledWith('item-1');
  });

  it('FLOW B — multi-item cart: two lines, update one, remove the other', async () => {
    const queryClient = newClient();
    const itemB = { ...cartItemFixture(), id: 'item-2', sku: 'SP-LC-002', productName: 'Lion\u2019s Mane Culture', quantity: 3, lineTotal: 747 };
    let current = makeCartResponse({
      itemCount: 4,
      subtotal: 996,
      items: [cartItemFixture(), itemB],
    });
    vi.mocked(cartApi.getCart).mockImplementation(() => Promise.resolve(current));
    vi.mocked(cartApi.updateItemQuantity).mockImplementation(async (_itemId, { quantity }) => {
      current = makeCartResponse({
        itemCount: quantity + 3,
        subtotal: 249 * quantity + 747,
        items: [{ ...cartItemFixture(), quantity, lineTotal: 249 * quantity }, itemB],
      });
      return current;
    });
    vi.mocked(cartApi.removeItem).mockImplementation(async (itemId) => {
      const remaining = current.data.items.filter((i) => i.id !== itemId);
      current = makeCartResponse({
        itemCount: remaining.reduce((sum, i) => sum + i.quantity, 0),
        subtotal: remaining.reduce((sum, i) => sum + i.lineTotal, 0),
        items: remaining,
      });
      return current;
    });

    renderApp(queryClient, '/cart');

    // Both lines render from backend data.
    await screen.findByTestId('cart-layout');
    expect(screen.getAllByTestId('cart-item')).toHaveLength(2);
    expect(screen.getByTestId('cart-summary-item-count')).toHaveTextContent('4');

    // Update product A (item-1).
    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Culture' }));
    await waitFor(() => {
      expect(screen.getByText(/Line total: ₹\s*498\.00/)).toBeInTheDocument();
      expect(screen.getByTestId('cart-summary-item-count')).toHaveTextContent('5');
    });

    // Remove product B (item-2).
    fireEvent.click(screen.getByRole('button', { name: 'Remove Lion\u2019s Mane Culture from cart' }));
    await waitFor(() => {
      expect(screen.getAllByTestId('cart-item')).toHaveLength(1);
      expect(screen.queryByRole('link', { name: 'Lion\u2019s Mane Culture' })).not.toBeInTheDocument();
    });
    expect(screen.getByTestId('cart-summary-total')).toHaveTextContent(/₹\s*498\.00/);
  });

  it('FLOW C — quantity update failure never falsely reports success and preserves totals', async () => {
    const queryClient = newClient();
    const current = makeCartResponse({ itemCount: 1, subtotal: 249, items: [cartItemFixture()] });
    vi.mocked(cartApi.getCart).mockImplementation(() => Promise.resolve(current));
    vi.mocked(cartApi.updateItemQuantity).mockRejectedValue(
      new ApiError('Quantity must be at least 1', 'CART_INVALID_QUANTITY', 400)
    );

    renderApp(queryClient, '/cart');
    await screen.findByTestId('cart-layout');

    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Culture' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('not allowed');
    // Value and totals remain server-confirmed.
    expect(screen.getByRole('spinbutton', { name: 'Quantity for Blue Oyster Culture' })).toHaveValue(1);
    expect(screen.getByTestId('cart-summary-total')).toHaveTextContent(/₹\s*249\.00/);
    expect(screen.queryByText('Added to Cart')).not.toBeInTheDocument();
  });

  it('FLOW D — out-of-stock line: warning shown, checkout blocked, removable', async () => {
    const queryClient = newClient();
    let current = makeCartResponse({ itemCount: 1, subtotal: 249, items: [cartItemFixture()] });
    vi.mocked(cartApi.getCart).mockImplementation(() => Promise.resolve(current));
    vi.mocked(cartApi.updateItemQuantity).mockRejectedValue(
      new ApiError('Product status is OUT_OF_STOCK', 'CATALOG_PRODUCT_NOT_PURCHASABLE', 400)
    );
    vi.mocked(cartApi.removeItem).mockImplementation(async () => {
      current = makeCartResponse({ itemCount: 0, items: [], subtotal: 0 });
      return current;
    });

    renderApp(queryClient, '/cart');
    await screen.findByTestId('cart-layout');

    fireEvent.click(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Culture' }));
    expect(await screen.findByTestId('cart-item-unavailable')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Proceed to Checkout' })).toHaveAttribute('href', '/checkout');
    expect(screen.getByRole('button', { name: 'Increase quantity for Blue Oyster Culture' })).toBeDisabled();

    // Removing the unavailable line resolves the cart.
    fireEvent.click(screen.getByRole('button', { name: 'Remove Blue Oyster Culture from cart' }));
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();
  });

  it('FLOW E — empty cart shows the empty state and Continue Shopping returns to the catalog', async () => {
    const queryClient = newClient();
    vi.mocked(cartApi.getCart).mockResolvedValue(
      makeCartResponse({ itemCount: 0, items: [], subtotal: 0 })
    );

    renderApp(queryClient, '/cart');
    expect(await screen.findByTestId('cart-empty')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('link', { name: 'Continue Shopping' }));
    expect(await screen.findByTestId('products-grid')).toBeInTheDocument();
  });
});
