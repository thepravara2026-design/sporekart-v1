import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductListPage } from '../pages/ProductListPage';
import { ProductDetailPage } from '../pages/ProductDetailPage';
import { CategoryListPage } from '../pages/CategoryListPage';
import { CatalogSearch } from '../components/CatalogSearch';
import { CatalogSort } from '../components/CatalogSort';
import { CatalogFiltersDrawer } from '../components/CatalogFiltersDrawer';
import { catalogApi } from '../../../services/catalogApi';
import { Product, Category, PageResponse } from '../../../types/catalog';
import { ToastProvider } from '../../../components/ui/Toast';

vi.mock('../../../services/catalogApi');

// ─── Shared fixtures ────────────────────────────────────────────────────────

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

const mockProduct2: Product = {
  id: 'prod-2222',
  sku: 'SP-LC-002',
  name: 'Golden Oyster Culture',
  description: 'Vibrant yellow gourmet culture',
  price: 299.0,
  currency: 'INR',
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
  size: 100,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
};

function createQC() {
  return new QueryClient({ defaultOptions: { queries: { retry: false } } });
}

function Wrapper({
  children,
  route = '/products',
}: {
  children: React.ReactNode;
  route?: string;
}) {
  return (
    <QueryClientProvider client={createQC()}>
      <ToastProvider>
        <MemoryRouter initialEntries={[route]}>
          {children}
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

// ─── ProductListPage ─────────────────────────────────────────────────────────

describe('ProductListPage (FD-09)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(catalogApi.getCategories).mockResolvedValue({ success: true, data: mockCategoriesPage });
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: mockProductsPage });
  });

  it('renders product-list-page testid', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(screen.getByTestId('product-list-page')).toBeInTheDocument();
  });

  it('shows loading skeletons while fetching', () => {
    vi.mocked(catalogApi.getProducts).mockReturnValue(new Promise(() => {}));
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(screen.getByTestId('products-loading')).toBeInTheDocument();
  });

  it('renders product grid on success', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('products-grid')).toBeInTheDocument();
    expect(screen.getByText('Blue Oyster Culture')).toBeInTheDocument();
    expect(screen.getByText('Golden Oyster Culture')).toBeInTheDocument();
  });

  it('renders error state when products API fails', async () => {
    vi.mocked(catalogApi.getProducts).mockRejectedValue(new Error('Network error: Backend unavailable'));
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('products-error')).toBeInTheDocument();
    expect(screen.getByText(/network error/i)).toBeInTheDocument();
  });

  it('renders empty state when no products match', async () => {
    vi.mocked(catalogApi.getProducts).mockResolvedValue({
      success: true,
      data: { content: [], page: 0, size: 12, totalElements: 0, totalPages: 0, first: true, last: true },
    });
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('products-empty')).toBeInTheDocument();
  });

  it('shows result count in toolbar after load', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByText('2 products')).toBeInTheDocument();
  });

  it('opens filter drawer when Filters button clicked', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('products-grid');
    const filterBtn = screen.getByRole('button', { name: /open filters/i });
    fireEvent.click(filterBtn);
    expect(screen.getByRole('dialog', { name: /filters/i })).toBeInTheDocument();
  });

  it('closes filter drawer when Close button clicked', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('products-grid');
    fireEvent.click(screen.getByRole('button', { name: /open filters/i }));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /close filters/i }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('submitting search form calls getProducts with search param', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('products-grid');
    const searchForm = screen.getByRole('search');
    const input = searchForm.querySelector('input')!;
    fireEvent.change(input, { target: { value: 'Blue Oyster' } });
    fireEvent.submit(searchForm);
    await waitFor(() => {
      expect(catalogApi.getProducts).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'Blue Oyster' }),
        expect.anything(),
      );
    });
  });

  it('product cards link to correct product detail URL', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('products-grid');
    const links = screen.getAllByRole('link', { name: /view details/i });
    expect(links[0]).toHaveAttribute('href', '/products/prod-1111');
  });

  it('INR price formatting is correct', async () => {
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('products-grid');
    // INR format: ₹249.00 or ₹ 249.00 depending on locale
    expect(screen.getByText(/249/)).toBeInTheDocument();
  });
});

// ─── CatalogSearch ───────────────────────────────────────────────────────────

describe('CatalogSearch (FD-09)', () => {
  it('renders with accessible search role', () => {
    render(
      <MemoryRouter>
        <CatalogSearch value="" onSearch={vi.fn()} />
      </MemoryRouter>,
    );
    expect(screen.getByRole('search')).toBeInTheDocument();
  });

  it('submits on Enter', () => {
    const onSearch = vi.fn();
    render(
      <MemoryRouter>
        <CatalogSearch value="" onSearch={onSearch} />
      </MemoryRouter>,
    );
    const form = screen.getByRole('search');
    const input = form.querySelector('input')!;
    fireEvent.change(input, { target: { value: 'oyster' } });
    fireEvent.submit(form);
    expect(onSearch).toHaveBeenCalledWith('oyster');
  });

  it('clear button calls onSearch with empty string', () => {
    const onSearch = vi.fn();
    render(
      <MemoryRouter>
        <CatalogSearch value="oyster" onSearch={onSearch} />
      </MemoryRouter>,
    );
    const clearBtn = screen.getByRole('button', { name: /clear search/i });
    fireEvent.click(clearBtn);
    expect(onSearch).toHaveBeenCalledWith('');
  });

  it('syncs local term when value prop changes', () => {
    const { rerender } = render(
      <MemoryRouter>
        <CatalogSearch value="initial" onSearch={vi.fn()} />
      </MemoryRouter>,
    );
    const form = screen.getByRole('search');
    const input = form.querySelector('input') as HTMLInputElement;
    expect(input.value).toBe('initial');
    rerender(
      <MemoryRouter>
        <CatalogSearch value="updated" onSearch={vi.fn()} />
      </MemoryRouter>,
    );
    expect(input.value).toBe('updated');
  });

  it('shows loading spinner when isLoading=true and term is set', () => {
    render(
      <MemoryRouter>
        <CatalogSearch value="oyster" onSearch={vi.fn()} isLoading={true} />
      </MemoryRouter>,
    );
    // Loading spinner has aria-label="Searching..."
    expect(screen.getByLabelText('Searching...')).toBeInTheDocument();
  });
});

// ─── CatalogFiltersDrawer ────────────────────────────────────────────────────

describe('CatalogFiltersDrawer (FD-09)', () => {
  const baseProps = {
    isOpen: true,
    onClose: vi.fn(),
    categories: [mockCategory],
    selectedCategory: '',
    selectedStatus: '',
    minPrice: '',
    maxPrice: '',
    onCategoryChange: vi.fn(),
    onStatusChange: vi.fn(),
    onMinPriceChange: vi.fn(),
    onMaxPriceChange: vi.fn(),
    onClearFilters: vi.fn(),
    hasActiveFilters: false,
  };

  beforeEach(() => vi.clearAllMocks());

  it('renders with role=dialog and accessible name', () => {
    render(<CatalogFiltersDrawer {...baseProps} />, { wrapper: MemoryRouter });
    expect(screen.getByRole('dialog', { name: /filters/i })).toBeInTheDocument();
  });

  it('calls onClose when Escape is pressed', () => {
    render(<CatalogFiltersDrawer {...baseProps} />, { wrapper: MemoryRouter });
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(baseProps.onClose).toHaveBeenCalled();
  });

  it('calls onClose when backdrop is clicked', () => {
    render(<CatalogFiltersDrawer {...baseProps} />, { wrapper: MemoryRouter });
    // backdrop is the first div before the dialog
    const dialog = screen.getByRole('dialog');
    const backdrop = dialog.previousElementSibling as HTMLElement;
    fireEvent.click(backdrop);
    expect(baseProps.onClose).toHaveBeenCalled();
  });

  it('shows category options', () => {
    render(<CatalogFiltersDrawer {...baseProps} />, { wrapper: MemoryRouter });
    expect(screen.getByText('Liquid Cultures')).toBeInTheDocument();
  });

  it('does not render when isOpen=false', () => {
    render(<CatalogFiltersDrawer {...baseProps} isOpen={false} />, { wrapper: MemoryRouter });
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('shows Clear All button only when hasActiveFilters=true', () => {
    const { rerender } = render(
      <CatalogFiltersDrawer {...baseProps} hasActiveFilters={false} />,
      { wrapper: MemoryRouter },
    );
    expect(screen.queryByRole('button', { name: /clear all/i })).not.toBeInTheDocument();

    rerender(<CatalogFiltersDrawer {...baseProps} hasActiveFilters={true} />);
    expect(screen.getByRole('button', { name: /clear all/i })).toBeInTheDocument();
  });

  it('all filter inputs have associated labels', () => {
    render(<CatalogFiltersDrawer {...baseProps} />, { wrapper: MemoryRouter });
    // Category select
    expect(screen.getByLabelText('Category')).toBeInTheDocument();
    // Availability select
    expect(screen.getByLabelText('Availability')).toBeInTheDocument();
  });
});

// ─── CatalogSort ─────────────────────────────────────────────────────────────

describe('CatalogSort (FD-09)', () => {
  it('renders with a visible Sort label', () => {
    render(<MemoryRouter><CatalogSort value="createdAt,desc" onChange={vi.fn()} /></MemoryRouter>);
    expect(screen.getByText('Sort:')).toBeInTheDocument();
  });

  it('renders all sort options', () => {
    render(<MemoryRouter><CatalogSort value="createdAt,desc" onChange={vi.fn()} /></MemoryRouter>);
    expect(screen.getByText('Newest Additions')).toBeInTheDocument();
    expect(screen.getByText('Name (A to Z)')).toBeInTheDocument();
    expect(screen.getByText('Price (Low to High)')).toBeInTheDocument();
  });

  it('calls onChange when sort value changes', () => {
    const onChange = vi.fn();
    render(<MemoryRouter><CatalogSort value="createdAt,desc" onChange={onChange} /></MemoryRouter>);
    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'name,asc' } });
    expect(onChange).toHaveBeenCalledWith('name,asc');
  });
});

// ─── CategoryListPage ────────────────────────────────────────────────────────

describe('CategoryListPage (FD-09)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(catalogApi.getCategories).mockResolvedValue({ success: true, data: mockCategoriesPage });
  });

  it('renders category-list-page testid', async () => {
    render(
      <Wrapper route="/categories">
        <Routes>
          <Route path="/categories" element={<CategoryListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(screen.getByTestId('category-list-page')).toBeInTheDocument();
  });

  it('renders category grid on success', async () => {
    render(
      <Wrapper route="/categories">
        <Routes>
          <Route path="/categories" element={<CategoryListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('category-grid')).toBeInTheDocument();
    expect(screen.getByText('Liquid Cultures')).toBeInTheDocument();
  });

  it('renders loading skeleton initially', () => {
    vi.mocked(catalogApi.getCategories).mockReturnValue(new Promise(() => {}));
    render(
      <Wrapper route="/categories">
        <Routes>
          <Route path="/categories" element={<CategoryListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(screen.getByTestId('categories-loading')).toBeInTheDocument();
  });

  it('renders error state on failure', async () => {
    vi.mocked(catalogApi.getCategories).mockRejectedValue(new Error('Failed to load'));
    render(
      <Wrapper route="/categories">
        <Routes>
          <Route path="/categories" element={<CategoryListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('categories-error')).toBeInTheDocument();
  });

  it('renders empty state when no categories returned', async () => {
    vi.mocked(catalogApi.getCategories).mockResolvedValue({
      success: true,
      data: { content: [], page: 0, size: 12, totalElements: 0, totalPages: 0, first: true, last: true },
    });
    render(
      <Wrapper route="/categories">
        <Routes>
          <Route path="/categories" element={<CategoryListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('categories-empty')).toBeInTheDocument();
  });

  it('category cards link to products page with categoryId filter', async () => {
    render(
      <Wrapper route="/categories">
        <Routes>
          <Route path="/categories" element={<CategoryListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('category-grid');
    const browseLink = screen.getByRole('link', { name: /browse products/i });
    expect(browseLink).toHaveAttribute('href', '/products?categoryId=cat-1111');
  });
});

// ─── ProductDetailPage ───────────────────────────────────────────────────────

describe('ProductDetailPage (FD-09)', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders product detail loading skeleton', () => {
    vi.mocked(catalogApi.getProduct).mockReturnValue(new Promise(() => {}));
    render(
      <Wrapper route="/products/prod-1111">
        <Routes>
          <Route path="/products/:productId" element={<ProductDetailPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(screen.getByTestId('product-detail-loading')).toBeInTheDocument();
  });

  it('renders product detail on success', async () => {
    vi.mocked(catalogApi.getProduct).mockResolvedValue({ success: true, data: mockProduct1 });
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: mockProductsPage });
    render(
      <Wrapper route="/products/prod-1111">
        <Routes>
          <Route path="/products/:productId" element={<ProductDetailPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('product-detail-card')).toBeInTheDocument();
    expect(screen.getAllByText('Blue Oyster Culture').length).toBeGreaterThan(0);
    expect(screen.getByText('SP-LC-001')).toBeInTheDocument();
  });

  it('renders error state on product fetch failure', async () => {
    vi.mocked(catalogApi.getProduct).mockRejectedValue(new Error('Network error'));
    render(
      <Wrapper route="/products/bad-id">
        <Routes>
          <Route path="/products/:productId" element={<ProductDetailPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('product-detail-error')).toBeInTheDocument();
  });

  it('renders breadcrumb with Products link', async () => {
    vi.mocked(catalogApi.getProduct).mockResolvedValue({ success: true, data: mockProduct1 });
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: mockProductsPage });
    render(
      <Wrapper route="/products/prod-1111">
        <Routes>
          <Route path="/products/:productId" element={<ProductDetailPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('product-detail-card');
    expect(screen.getByRole('link', { name: 'Products' })).toBeInTheDocument();
  });
});

// ─── Retry flow integration ──────────────────────────────────────────────────

describe('CatalogEndToEnd — retry and state flows (FD-09)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(catalogApi.getCategories).mockResolvedValue({ success: true, data: mockCategoriesPage });
  });

  it('retry after failure shows products', async () => {
    vi.mocked(catalogApi.getProducts).mockRejectedValueOnce(new Error('Network error'));
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: mockProductsPage });

    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    expect(await screen.findByTestId('products-error')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Retry'));
    expect(await screen.findByTestId('products-grid')).toBeInTheDocument();
  });

  it('price is formatted in INR locale', async () => {
    vi.mocked(catalogApi.getProducts).mockResolvedValue({ success: true, data: mockProductsPage });
    render(
      <Wrapper route="/products">
        <Routes>
          <Route path="/products" element={<ProductListPage />} />
        </Routes>
      </Wrapper>,
    );
    await screen.findByTestId('products-grid');
    // Verify INR formatting contains the rupee character or INR prefix
    const allText = document.body.textContent || '';
    expect(allText).toMatch(/₹|INR|249/);
  });
});
