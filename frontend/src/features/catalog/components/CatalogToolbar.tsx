import { FC, ReactNode } from 'react';
import { CatalogSearch } from './CatalogSearch';
import { CatalogSort } from './CatalogSort';
import { X } from 'lucide-react';

export interface ActiveFilterChip {
  label: string;
  onRemove: () => void;
}

export interface CatalogToolbarProps {
  search: string;
  onSearch: (value: string) => void;
  sort: string;
  onSort: (value: string) => void;
  totalElements?: number;
  isLoading?: boolean;
  filterTrigger?: ReactNode;
  activeFilters?: ActiveFilterChip[];
  className?: string;
}

export const CatalogToolbar: FC<CatalogToolbarProps> = ({
  search,
  onSearch,
  sort,
  onSort,
  totalElements,
  isLoading = false,
  filterTrigger,
  activeFilters = [],
  className = '',
}) => {
  return (
    <div
      className={`catalog-toolbar ${className}`}
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem',
        marginBottom: '1.5rem',
      }}
    >
      {/* Top row: search + controls */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          gap: '0.75rem',
        }}
      >
        {/* search — flex grows */}
        <div style={{ flex: 1, minWidth: '240px', maxWidth: '480px' }}>
          <CatalogSearch value={search} onSearch={onSearch} isLoading={isLoading} />
        </div>

        {/* right controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          {filterTrigger}
          <CatalogSort value={sort} onChange={onSort} />
        </div>
      </div>

      {/* Result count + active filter chips row */}
      {(totalElements !== undefined || activeFilters.length > 0) && (
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          {totalElements !== undefined && (
            <span
              style={{
                fontSize: '0.825rem',
                color: 'var(--text-secondary)',
                fontWeight: 600,
                marginRight: '0.25rem',
              }}
              aria-live="polite"
              aria-atomic="true"
            >
              {isLoading ? 'Searching…' : `${totalElements} ${totalElements === 1 ? 'product' : 'products'}`}
            </span>
          )}

          {activeFilters.map((chip, i) => (
            <span
              key={i}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.35rem',
                padding: '0.2rem 0.6rem 0.2rem 0.75rem',
                background: 'rgba(16, 185, 129, 0.12)',
                border: '1px solid rgba(16, 185, 129, 0.3)',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.775rem',
                color: 'var(--accent-primary)',
                fontWeight: 600,
              }}
            >
              {chip.label}
              <button
                type="button"
                aria-label={`Remove filter: ${chip.label}`}
                onClick={chip.onRemove}
                style={{
                  background: 'none',
                  border: 'none',
                  padding: '0',
                  cursor: 'pointer',
                  color: 'var(--accent-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  lineHeight: 1,
                }}
              >
                <X size={12} aria-hidden="true" />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
};
