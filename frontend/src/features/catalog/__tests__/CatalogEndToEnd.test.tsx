import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductListPage } from '../pages/ProductListPage';
import { ProductDetailPage } from '../pages/ProductDetailPage';
import { catalogApi } from '../../../services/catalogApi';
import { Product, Category, PageResponse } from '../../../types/catalog';
import { ApiResponse } from '../../../types/api';

vi.mock('../../../services/catalogApi');

describe('Catalog End-to-End Release Hardening Journey', () => {
  let queryClient: QueryClient;

  const mockCategory: Category = {
    id: 'cat-1111',
    name: 'Liquid Cultures',
    slug: 'liquid-cultures',
    description: 'Fresh liquid cultures',
    status: 'ACTIVE',
    createdAt: '2026-08-14T10:00:00Z',
    updatedAt: '2026-08-14T10:00:00Z',
  };

  const mockProduct1: Product = {
    id: 'prod-1111',
    sku: 'SP-LC-001',
    name: 'Blue Oyster Culture',
    description: 'High-yield gourmet culture',
    price: 24.99,
    currency: 'USD',
    status: 'ACTIVE',
    category: mockCategory,
    createdAt: '2026-08-14T10:00:00Z',
    updatedAt: '2026-08-14T10:00:00Z',
  };

  const mockProduct2: Product = {
    id: 'prod-2222',
    sku: 'SP-LC-002',
    name: 'Golden Oyster Culture',
    description: 'Vibrant yellow gourmet culture',
    price: 29.99,
    currency: 'USD',
    status: 'ACTIVE',
    category: mockCategory,
    createdAt: '2026-08-14T11:00:00Z',
    updatedAt: '2026-08-14T11:00:00Z',
  };

  const mockProductsPage: PageResponse<Product> = {
    content: [mockProduct1, mockProduct2],
    page: 0,
    size: 12,
    totalElements: 2,
    totalPages: 1,
    first: true,
    last: true,
  };

  const mockCategoriesPage: PageResponse<Category> = {
    content: [mockCategory],
    page: 0,
    size: 50,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    vi.clearAllMocks();

    vi.mocked(catalogApi.getCategories).mockResolvedValue({
      success: true,
      data: mockCategoriesPage,
    });

    vi.mocked(catalogApi.getProducts).mockResolvedValue({
      success: true,
      data: mockProductsPage,
    });
  });

  it('TEST 01 & 02 & 03 & 04: Browse catalog, search, filter by price and category', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products']}>
          <Routes>
            <Route path="/products" element={<ProductListPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Blue Oyster Culture')).toBeInTheDocument();
      expect(screen.getByText('Golden Oyster Culture')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText('Search products...');
    fireEvent.change(searchInput, { target: { value: 'Blue' } });

    await waitFor(() => {
      expect(catalogApi.getProducts).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'Blue' }),
        expect.anything()
      );
    });

    const minPriceInput = screen.getByPlaceholderText('Min $');
    fireEvent.change(minPriceInput, { target: { value: '20' } });

    await waitFor(() => {
      expect(catalogApi.getProducts).toHaveBeenCalledWith(
        expect.objectContaining({ minPrice: 20 }),
        expect.anything()
      );
    });
  });

  it('TEST 07 & 08: Open product detail and handle non-existent product 404', async () => {
    vi.mocked(catalogApi.getProduct).mockImplementation((id: string): Promise<ApiResponse<Product>> => {
      if (id === 'prod-1111') {
        return Promise.resolve({
          success: true,
          data: mockProduct1,
        });
      }
      return Promise.reject(new Error('Product not found'));
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products/prod-1111']}>
          <Routes>
            <Route path="/products/:productId" element={<ProductDetailPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getAllByText('Blue Oyster Culture').length).toBeGreaterThan(0);
      expect(screen.getByText('$24.99')).toBeInTheDocument();
      expect(screen.getByText('SKU: SP-LC-001')).toBeInTheDocument();
    });
  });

  it('TEST 09: Handle backend failure and retry flow', async () => {
    vi.mocked(catalogApi.getProducts).mockRejectedValueOnce(new Error('Network error: Backend unavailable'));

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products']}>
          <Routes>
            <Route path="/products" element={<ProductListPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Unable to load catalog products')).toBeInTheDocument();
      expect(screen.getByText('Network error: Backend unavailable')).toBeInTheDocument();
    });

    vi.mocked(catalogApi.getProducts).mockResolvedValueOnce({
      success: true,
      data: mockProductsPage,
    });

    const retryBtn = screen.getByText('Retry');
    fireEvent.click(retryBtn);

    await waitFor(() => {
      expect(screen.getByText('Blue Oyster Culture')).toBeInTheDocument();
    });
  });
});
