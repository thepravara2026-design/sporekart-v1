import { FC, useId } from 'react';
import { SORT_OPTIONS } from '../constants/catalogConstants';

export interface CatalogSortProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

export const CatalogSort: FC<CatalogSortProps> = ({ value, onChange, className = '' }) => {
  const selectId = useId();

  return (
    <div
      className={`catalog-sort-wrapper ${className}`}
      style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', minWidth: '200px' }}
    >
      <label
        htmlFor={selectId}
        style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap', fontWeight: 600 }}
      >
        Sort:
      </label>
      <select
        id={selectId}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{
          flex: 1,
          height: '2.5rem',
          background: 'var(--surface-elevated)',
          border: '1px solid var(--border-primary)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--text-primary)',
          fontSize: '0.875rem',
          padding: '0 0.75rem',
          cursor: 'pointer',
          outline: 'none',
        }}
      >
        {SORT_OPTIONS.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
};
