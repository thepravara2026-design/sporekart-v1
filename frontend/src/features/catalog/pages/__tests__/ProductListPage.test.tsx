import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductListPage } from '../ProductListPage';
import { catalogApi } from '../../../../services/catalogApi';
import { Product, Category, PageResponse } from '../../../../types/catalog';
import { ApiResponse } from '../../../../types/api';

vi.mock('../../../../services/catalogApi', () => ({
  catalogApi: {
    getProducts: vi.fn(),
    getCategories: vi.fn(),
  },
}));

describe('ProductListPage', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    vi.clearAllMocks();
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
  });

  const mockCategory: Category = {
    id: 'cat-1',
    name: 'Medicinal',
    slug: 'medicinal',
    description: null,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  const mockProduct: Product = {
    id: 'prod-1',
    sku: 'SKU-001',
    name: 'Shiitake Culture',
    description: 'Organic shiitake spawn',
    price: 19.99,
    currency: 'INR',
    status: 'ACTIVE',
    category: mockCategory,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  const mockCategoryPage: ApiResponse<PageResponse<Category>> = {
    success: true,
    data: {
      content: [mockCategory],
      page: 0,
      size: 50,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    },
  };

  const mockProductPage: ApiResponse<PageResponse<Product>> = {
    success: true,
    data: {
      content: [mockProduct],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    },
  };

  it('renders products list cleanly when query succeeds', async () => {
    (catalogApi.getCategories as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockCategoryPage);
    (catalogApi.getProducts as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockProductPage);

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products']}>
          <ProductListPage />
        </MemoryRouter>
      </QueryClientProvider>
    );

    // The new ProductListPage uses CatalogToolbar + CatalogFiltersDrawer instead of CatalogFilterBar
    expect(screen.getByTestId('product-list-page')).toBeInTheDocument();
    expect(await screen.findByText('Shiitake Culture')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-001')).toBeInTheDocument();
  });

  it('renders empty state when no products returned', async () => {
    (catalogApi.getCategories as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockCategoryPage);
    (catalogApi.getProducts as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: {
        content: [],
        page: 0,
        size: 12,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
      },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products']}>
          <ProductListPage />
        </MemoryRouter>
      </QueryClientProvider>
    );

    expect(await screen.findByTestId('products-empty')).toBeInTheDocument();
    expect(screen.getByText('No products found')).toBeInTheDocument();
  });
});
