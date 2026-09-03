import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CatalogFilterBar } from '../CatalogFilterBar';

describe('CatalogFilterBar Component', () => {
  const mockCategories = [
    {
      id: 'cat-1',
      name: 'Liquid Cultures',
      slug: 'liquid-cultures',
      description: 'Desc',
      status: 'ACTIVE' as const,
      createdAt: '2026-08-14T00:00:00Z',
      updatedAt: '2026-08-14T00:00:00Z',
    },
  ];

  it('renders search input, category dropdown, price inputs, and triggers callbacks', () => {
    const onSearchChange = vi.fn();
    const onCategoryChange = vi.fn();
    const onStatusChange = vi.fn();
    const onSortChange = vi.fn();
    const onMinPriceChange = vi.fn();
    const onMaxPriceChange = vi.fn();
    const onClearFilters = vi.fn();

    render(
      <CatalogFilterBar
        categories={mockCategories}
        search=""
        selectedCategory=""
        selectedStatus=""
        selectedSort="createdAt,desc"
        minPrice=""
        maxPrice=""
        onSearchChange={onSearchChange}
        onCategoryChange={onCategoryChange}
        onStatusChange={onStatusChange}
        onSortChange={onSortChange}
        onMinPriceChange={onMinPriceChange}
        onMaxPriceChange={onMaxPriceChange}
        onClearFilters={onClearFilters}
      />
    );

    const searchInput = screen.getByPlaceholderText('Search products...');
    fireEvent.change(searchInput, { target: { value: 'oyster' } });
    expect(onSearchChange).toHaveBeenCalledWith('oyster');

    const minPriceInput = screen.getByPlaceholderText(/Min/i);
    fireEvent.change(minPriceInput, { target: { value: '10' } });
    expect(onMinPriceChange).toHaveBeenCalledWith('10');

    const maxPriceInput = screen.getByPlaceholderText(/Max/i);
    fireEvent.change(maxPriceInput, { target: { value: '50' } });
    expect(onMaxPriceChange).toHaveBeenCalledWith('50');
  });

  it('shows clear filters button when active filters exist', () => {
    const onClearFilters = vi.fn();

    render(
      <CatalogFilterBar
        categories={mockCategories}
        search="oyster"
        selectedCategory=""
        selectedStatus=""
        selectedSort="createdAt,desc"
        onSearchChange={() => {}}
        onCategoryChange={() => {}}
        onStatusChange={() => {}}
        onSortChange={() => {}}
        onClearFilters={onClearFilters}
      />
    );

    const clearBtn = screen.getByText('Clear Filters');
    expect(clearBtn).toBeInTheDocument();
    fireEvent.click(clearBtn);
    expect(onClearFilters).toHaveBeenCalled();
  });
});
