import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import {
  ProductPrice,
  ProductAvailability,
  ProductImage,
  ProductCard,
  ProductCardSkeleton,
  ProductGrid,
  CategoryCard,
  CategoryGrid,
  CatalogSearch,
  CatalogSort,
  CatalogFilters,
  CatalogToolbar,
  CatalogEmptyState,
  CatalogErrorState,
} from '../../index';
import { Product, Category } from '../../types/catalog';

const mockProduct: Product = {
  id: 'prod-101',
  sku: 'SPW-OYS-001',
  name: 'Florida White Oyster Spawn',
  description: 'High-yielding oyster mushroom spawn on sterile grain.',
  price: 24.99,
  currency: 'USD',
  status: 'ACTIVE',
  category: {
    id: 'cat-1',
    name: 'Oyster Mushroom Spawn',
    slug: 'oyster-spawn',
    description: 'All oyster strains',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

const mockCategory: Category = {
  id: 'cat-1',
  name: 'Oyster Mushroom Spawn',
  slug: 'oyster-spawn',
  description: 'All oyster strains',
  status: 'ACTIVE',
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('Catalog Feature Components (Sprint FD-06)', () => {
  it('renders ProductPrice formatted currency correctly', () => {
    render(<ProductPrice price={24.99} currency="USD" />);
    expect(screen.getByText('$24.99')).toBeDefined();
  });

  it('renders ProductAvailability with badge mapping', () => {
    render(<ProductAvailability status="ACTIVE" />);
    expect(screen.getByText('In Stock')).toBeDefined();
  });

  it('renders ProductImage with alt text and fallback icon', () => {
    render(<ProductImage alt="Test Product" />);
    expect(screen.getByText('Mushroom Spawn')).toBeDefined();
  });

  it('renders ProductCard with product details and Add to Cart action', () => {
    const handleAddToCart = vi.fn();
    render(
      <BrowserRouter>
        <ProductCard product={mockProduct} onAddToCart={handleAddToCart} />
      </BrowserRouter>
    );

    expect(screen.getByText('Florida White Oyster Spawn')).toBeDefined();
    expect(screen.getByText('$24.99')).toBeDefined();
    const cartBtn = screen.getByRole('button', { name: 'Add to Cart' });
    fireEvent.click(cartBtn);
    expect(handleAddToCart).toHaveBeenCalledWith(mockProduct);
  });

  it('renders ProductCardSkeleton loading state', () => {
    render(<ProductCardSkeleton />);
    expect(document.querySelector('.skeleton-loader')).toBeDefined();
  });

  it('renders ProductGrid with multiple product cards', () => {
    render(
      <BrowserRouter>
        <ProductGrid products={[mockProduct]} />
      </BrowserRouter>
    );
    expect(screen.getByText('Florida White Oyster Spawn')).toBeDefined();
  });

  it('renders CategoryCard and CategoryGrid', () => {
    render(
      <BrowserRouter>
        <CategoryCard category={mockCategory} />
        <CategoryGrid categories={[mockCategory]} />
      </BrowserRouter>
    );
    expect(screen.getAllByText('Oyster Mushroom Spawn').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Browse Products').length).toBeGreaterThan(0);
  });

  it('renders CatalogSearch and fires search callback on submit', () => {
    const handleSearch = vi.fn();
    render(<CatalogSearch value="Oyster" onSearch={handleSearch} />);
    // CatalogSearch uses role="search" form with a sr-only label
    const form = screen.getByRole('search');
    expect(form).toBeDefined();
    const submitBtn = screen.getByRole('button', { name: 'Submit search' });
    fireEvent.click(submitBtn);
    expect(handleSearch).toHaveBeenCalledWith('Oyster');
  });

  it('renders CatalogSort select dropdown', () => {
    const handleSortChange = vi.fn();
    render(<CatalogSort value="name,asc" onChange={handleSortChange} />);
    // CatalogSort uses a visible 'Sort:' label associated via htmlFor
    const select = screen.getByRole('combobox');
    expect(select).toBeDefined();
    fireEvent.change(select, { target: { value: 'price,asc' } });
    expect(handleSortChange).toHaveBeenCalledWith('price,asc');
  });

  it('renders CatalogFilters panel with category and status controls', () => {
    const handleReset = vi.fn();
    render(
      <CatalogFilters
        filters={{ search: '', categoryId: '', status: '', sort: 'name,asc', page: 0, size: 12 }}
        categories={[mockCategory]}
        onCategoryChange={vi.fn()}
        onStatusChange={vi.fn()}
        onPriceChange={vi.fn()}
        onReset={handleReset}
      />
    );
    expect(screen.getByText('Filter Catalog')).toBeDefined();
    const resetBtn = screen.getByRole('button', { name: 'Reset Filters' });
    fireEvent.click(resetBtn);
    expect(handleReset).toHaveBeenCalled();
  });

  it('renders CatalogToolbar with total count and search/sort components', () => {
    render(
      <CatalogToolbar
        search=""
        onSearch={vi.fn()}
        sort="name,asc"
        onSort={vi.fn()}
        totalElements={15}
      />
    );
    expect(screen.getByText('15 products')).toBeDefined();
  });

  it('renders CatalogEmptyState with reset action button', () => {
    const handleReset = vi.fn();
    render(<CatalogEmptyState onResetFilters={handleReset} />);
    expect(screen.getByText('No products found')).toBeDefined();
    const resetBtn = screen.getByRole('button', { name: 'Clear All Filters' });
    fireEvent.click(resetBtn);
    expect(handleReset).toHaveBeenCalled();
  });

  it('renders CatalogErrorState with retry action', () => {
    const handleRetry = vi.fn();
    render(<CatalogErrorState onRetry={handleRetry} />);
    expect(screen.getByRole('alert')).toBeDefined();
    const retryBtn = screen.getByRole('button', { name: 'Retry' });
    fireEvent.click(retryBtn);
    expect(handleRetry).toHaveBeenCalled();
  });
});
