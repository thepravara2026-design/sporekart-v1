import { FC, ChangeEvent } from 'react';
import { Category } from '../../../types/catalog';

interface CatalogFilterBarProps {
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

  return (
    <div className="catalog-filter-bar" data-testid="catalog-filter-bar">
      <div className="filter-group filter-search">
        <label htmlFor="search-input" className="filter-label">Search</label>
        <input
          id="search-input"
          type="text"
          className="form-control"
          placeholder="Search products..."
          value={search}
          onChange={(e: ChangeEvent<HTMLInputElement>) => onSearchChange(e.target.value)}
        />
      </div>

      <div className="filter-group">
        <label htmlFor="category-select" className="filter-label">Category</label>
        <select
          id="category-select"
          className="form-control"
          value={selectedCategory}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => onCategoryChange(e.target.value)}
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-group">
        <label htmlFor="status-select" className="filter-label">Availability</label>
        <select
          id="status-select"
          className="form-control"
          value={selectedStatus}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => onStatusChange(e.target.value)}
        >
          <option value="">All Statuses</option>
          <option value="ACTIVE">Active / In Stock</option>
          <option value="OUT_OF_STOCK">Out of Stock</option>
          <option value="DRAFT">Draft</option>
        </select>
      </div>

      {onMinPriceChange && (
        <div className="filter-group">
          <label htmlFor="min-price-input" className="filter-label">Min Price</label>
          <input
            id="min-price-input"
            type="number"
            min="0"
            step="0.01"
            className="form-control"
            placeholder="Min $"
            value={minPrice}
            onChange={(e: ChangeEvent<HTMLInputElement>) => onMinPriceChange(e.target.value)}
          />
        </div>
      )}

      {onMaxPriceChange && (
        <div className="filter-group">
          <label htmlFor="max-price-input" className="filter-label">Max Price</label>
          <input
            id="max-price-input"
            type="number"
            min="0"
            step="0.01"
            className="form-control"
            placeholder="Max $"
            value={maxPrice}
            onChange={(e: ChangeEvent<HTMLInputElement>) => onMaxPriceChange(e.target.value)}
          />
        </div>
      )}

      <div className="filter-group">
        <label htmlFor="sort-select" className="filter-label">Sort By</label>
        <select
          id="sort-select"
          className="form-control"
          value={selectedSort}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => onSortChange(e.target.value)}
        >
          <option value="createdAt,desc">Newest First</option>
          <option value="name,asc">Name: A to Z</option>
          <option value="name,desc">Name: Z to A</option>
          <option value="price,asc">Price: Low to High</option>
          <option value="price,desc">Price: High to Low</option>
        </select>
      </div>

      {hasActiveFilters && (
        <div className="filter-group filter-actions">
          <button type="button" className="btn btn-secondary btn-sm" onClick={onClearFilters}>
            Clear Filters
          </button>
        </div>
      )}
    </div>
  );
};
