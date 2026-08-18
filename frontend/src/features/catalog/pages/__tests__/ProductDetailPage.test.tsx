import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductDetailPage } from '../ProductDetailPage';
import { catalogApi } from '../../../../services/catalogApi';
import { Product } from '../../../../types/catalog';
import { ApiError } from '../../../../services/apiError';

vi.mock('../../../../services/catalogApi', () => ({
  catalogApi: {
    getProduct: vi.fn(),
  },
}));

describe('ProductDetailPage', () => {
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

  const mockProduct: Product = {
    id: 'prod-99',
    sku: 'SKU-TURKEY-001',
    name: 'Turkey Tail Extract',
    description: 'Immune support extract',
    price: 29.99,
    currency: 'USD',
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

  it('renders product details when query succeeds', async () => {
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: mockProduct,
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products/prod-99']}>
          <Routes>
            <Route path="/products/:productId" element={<ProductDetailPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    const titleElements = await screen.findAllByText('Turkey Tail Extract');
    expect(titleElements.length).toBeGreaterThan(0);
    expect(screen.getByText('SKU: SKU-TURKEY-001')).toBeInTheDocument();
    expect(screen.getByText('$29.99')).toBeInTheDocument();
    expect(screen.getByText('Immune support extract')).toBeInTheDocument();
  });

  it('renders 404 experience when product is not found', async () => {
    const error404 = new ApiError('Product not found', 'CATALOG_PRODUCT_NOT_FOUND', 404);
    (catalogApi.getProduct as unknown as ReturnType<typeof vi.fn>).mockRejectedValueOnce(error404);

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products/invalid-id']}>
          <Routes>
            <Route path="/products/:productId" element={<ProductDetailPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    expect(await screen.findByTestId('product-not-found')).toBeInTheDocument();
    expect(screen.getByText('Product Not Found')).toBeInTheDocument();
  });
});
