import { FC, ReactNode } from 'react';
import { CatalogSearch } from './CatalogSearch';
import { CatalogSort } from './CatalogSort';

export interface CatalogToolbarProps {
  search: string;
  onSearch: (value: string) => void;
  sort: string;
  onSort: (value: string) => void;
  totalElements?: number;
  filterTrigger?: ReactNode;
  className?: string;
}

export const CatalogToolbar: FC<CatalogToolbarProps> = ({
  search,
  onSearch,
  sort,
  onSort,
  totalElements,
  filterTrigger,
  className = '',
}) => {
  return (
    <div
      className={`catalog-toolbar card ${className}`}
      style={{
        padding: '1rem 1.25rem',
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '1rem',
        marginBottom: '1.5rem',
      }}
    >
      <div style={{ flex: 1, minWidth: '240px', maxWidth: '400px' }}>
        <CatalogSearch value={search} onSearch={onSearch} />
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        {totalElements !== undefined && (
          <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
            {totalElements} {totalElements === 1 ? 'product' : 'products'}
          </span>
        )}
        {filterTrigger}
        <CatalogSort value={sort} onChange={onSort} />
      </div>
    </div>
  );
};
