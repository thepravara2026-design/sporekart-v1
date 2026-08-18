import { FC } from 'react';
import { Category, CatalogFilterState, ProductStatus } from '../types/catalog';
import { FormField } from '../../../components/ui/FormField';
import { Input } from '../../../components/ui/Input';
import { Select } from '../../../components/ui/Select';
import { Button } from '../../../components/ui/Button';
import { RotateCcw } from 'lucide-react';

export interface CatalogFiltersProps {
  filters: CatalogFilterState;
  categories: Category[];
  onCategoryChange: (categoryId: string) => void;
  onStatusChange: (status: ProductStatus | '') => void;
  onPriceChange: (min?: number, max?: number) => void;
  onReset: () => void;
  className?: string;
}

export const CatalogFilters: FC<CatalogFiltersProps> = ({
  filters,
  categories,
  onCategoryChange,
  onStatusChange,
  onPriceChange,
  onReset,
  className = '',
}) => {
  const categoryOptions = [
    { value: '', label: 'All Categories' },
    ...categories.map((c) => ({ value: c.id, label: c.name })),
  ];

  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'ACTIVE', label: 'In Stock' },
    { value: 'OUT_OF_STOCK', label: 'Out of Stock' },
  ];

  return (
    <div className={`catalog-filters-panel card ${className}`} style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      <h3 style={{ fontSize: '1rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
        Filter Catalog
      </h3>

      <FormField label="Category" htmlFor="filter-category">
        <Select
          id="filter-category"
          value={filters.categoryId}
          onChange={(e) => onCategoryChange(e.target.value)}
          options={categoryOptions}
        />
      </FormField>

      <FormField label="Availability Status" htmlFor="filter-status">
        <Select
          id="filter-status"
          value={filters.status}
          onChange={(e) => onStatusChange(e.target.value as ProductStatus | '')}
          options={statusOptions}
        />
      </FormField>

      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <FormField label="Min Price ($)" htmlFor="filter-min-price">
          <Input
            id="filter-min-price"
            type="number"
            placeholder="Min $"
            value={filters.minPrice !== undefined ? filters.minPrice : ''}
            onChange={(e) => {
              const val = e.target.value ? parseFloat(e.target.value) : undefined;
              onPriceChange(val, filters.maxPrice);
            }}
          />
        </FormField>

        <FormField label="Max Price ($)" htmlFor="filter-max-price">
          <Input
            id="filter-max-price"
            type="number"
            placeholder="Max $"
            value={filters.maxPrice !== undefined ? filters.maxPrice : ''}
            onChange={(e) => {
              const val = e.target.value ? parseFloat(e.target.value) : undefined;
              onPriceChange(filters.minPrice, val);
            }}
          />
        </FormField>
      </div>

      <Button variant="secondary" size="sm" leftIcon={<RotateCcw size={14} />} onClick={onReset}>
        Reset Filters
      </Button>
    </div>
  );
};
