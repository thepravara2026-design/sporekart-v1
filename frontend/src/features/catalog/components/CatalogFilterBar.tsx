import { FC, ChangeEvent } from 'react';
import { Category } from '../types/catalog';
import { FormField } from '../../../components/ui/FormField';
import { Input } from '../../../components/ui/Input';
import { Select } from '../../../components/ui/Select';
import { Button } from '../../../components/ui/Button';
import { Search, X } from 'lucide-react';

export interface CatalogFilterBarProps {
  categories: Category[];
  search: string;
  selectedCategory: string;
  selectedStatus: string;
  selectedSort: string;
  minPrice?: string;
  maxPrice?: string;
  onSearchChange: (value: string) => void;
  onCategoryChange: (value: string) => void;
  onStatusChange: (value: string) => void;
  onSortChange: (value: string) => void;
  onMinPriceChange?: (value: string) => void;
  onMaxPriceChange?: (value: string) => void;
  onClearFilters: () => void;
}

export const CatalogFilterBar: FC<CatalogFilterBarProps> = ({
  categories,
  search,
  selectedCategory,
  selectedStatus,
  selectedSort,
  minPrice = '',
  maxPrice = '',
  onSearchChange,
  onCategoryChange,
  onStatusChange,
  onSortChange,
  onMinPriceChange,
  onMaxPriceChange,
  onClearFilters,
}) => {
  const hasActiveFilters = Boolean(
    search || selectedCategory || selectedStatus || minPrice || maxPrice || selectedSort !== 'createdAt,desc'
  );

  const categoryOptions = [
    { value: '', label: 'All Categories' },
    ...categories.map((cat) => ({ value: cat.id, label: cat.name })),
  ];

  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'ACTIVE', label: 'Active / In Stock' },
    { value: 'OUT_OF_STOCK', label: 'Out of Stock' },
    { value: 'DRAFT', label: 'Draft' },
  ];

  const sortOptions = [
    { value: 'createdAt,desc', label: 'Newest First' },
    { value: 'name,asc', label: 'Name: A to Z' },
    { value: 'name,desc', label: 'Name: Z to A' },
    { value: 'price,asc', label: 'Price: Low to High' },
    { value: 'price,desc', label: 'Price: High to Low' },
  ];

  return (
    <div
      className="catalog-filter-bar card"
      data-testid="catalog-filter-bar"
      style={{
        padding: '1.25rem',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '1rem',
        alignItems: 'end',
        marginBottom: '1.5rem',
      }}
    >
      <FormField label="Search" htmlFor="search-input">
        <div style={{ position: 'relative' }}>
          <Search
            size={16}
            aria-hidden="true"
            style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }}
          />
          <Input
            id="search-input"
            placeholder="Search products..."
            value={search}
            onChange={(e: ChangeEvent<HTMLInputElement>) => onSearchChange(e.target.value)}
            style={{ paddingLeft: '2.4rem', paddingRight: search ? '2.4rem' : '1rem' }}
          />
          {search && (
            <button
              type="button"
              aria-label="Clear search"
              onClick={() => onSearchChange('')}
              style={{
                position: 'absolute',
                right: '0.5rem',
                top: '50%',
                transform: 'translateY(-50%)',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: '1.6rem',
                height: '1.6rem',
                borderRadius: 'var(--radius-full)',
                background: 'rgba(255, 255, 255, 0.08)',
                border: '1px solid var(--border-color)',
                color: 'var(--text-secondary)',
                cursor: 'pointer',
                transition: 'background-color var(--transition-fast), color var(--transition-fast)',
              }}
            >
              <X size={13} aria-hidden="true" />
            </button>
          )}
        </div>
      </FormField>

      <FormField label="Category" htmlFor="category-select">
        <Select
          id="category-select"
          value={selectedCategory}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => onCategoryChange(e.target.value)}
          options={categoryOptions}
        />
      </FormField>

      <FormField label="Availability" htmlFor="status-select">
        <Select
          id="status-select"
          value={selectedStatus}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => onStatusChange(e.target.value)}
          options={statusOptions}
        />
      </FormField>

      {onMinPriceChange && (
        <FormField label="Min Price" htmlFor="min-price-input">
          <Input
            id="min-price-input"
            type="number"
            min="0"
            step="0.01"
            placeholder="Min ₹"
            value={minPrice}
            onChange={(e: ChangeEvent<HTMLInputElement>) => onMinPriceChange(e.target.value)}
          />
        </FormField>
      )}

      {onMaxPriceChange && (
        <FormField label="Max Price" htmlFor="max-price-input">
          <Input
            id="max-price-input"
            type="number"
            min="0"
            step="0.01"
            placeholder="Max ₹"
            value={maxPrice}
            onChange={(e: ChangeEvent<HTMLInputElement>) => onMaxPriceChange(e.target.value)}
          />
        </FormField>
      )}

      <FormField label="Sort By" htmlFor="sort-select">
        <Select
          id="sort-select"
          value={selectedSort}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => onSortChange(e.target.value)}
          options={sortOptions}
        />
      </FormField>

      {hasActiveFilters && (
        <div style={{ display: 'flex', alignItems: 'flex-end' }}>
          <Button variant="secondary" size="sm" onClick={onClearFilters} fullWidth>
            Clear Filters
          </Button>
        </div>
      )}
    </div>
  );
};
