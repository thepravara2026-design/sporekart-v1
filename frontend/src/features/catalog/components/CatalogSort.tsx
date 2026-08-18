import { FC } from 'react';
import { Select } from '../../../components/ui/Select';
import { SORT_OPTIONS } from '../constants/catalogConstants';

export interface CatalogSortProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

export const CatalogSort: FC<CatalogSortProps> = ({ value, onChange, className = '' }) => {
  return (
    <div className={`catalog-sort-wrapper ${className}`} style={{ minWidth: '180px' }}>
      <Select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        options={SORT_OPTIONS}
        aria-label="Sort products by"
      />
    </div>
  );
};
