import { FC } from 'react';
import { EmptyState } from '../../../components/ui/EmptyState';
import { Button } from '../../../components/ui/Button';
import { PackageOpen, RotateCcw } from 'lucide-react';

export interface CatalogEmptyStateProps {
  title?: string;
  description?: string;
  onResetFilters?: () => void;
  className?: string;
}

export const CatalogEmptyState: FC<CatalogEmptyStateProps> = ({
  title = 'No products found',
  description = 'No mushroom spawn or cultivation products match your search or filter parameters.',
  onResetFilters,
  className = '',
}) => {
  return (
    <EmptyState
      icon={<PackageOpen size={48} style={{ color: 'var(--accent-primary)' }} />}
      title={title}
      description={description}
      action={
        onResetFilters ? (
          <Button variant="primary" leftIcon={<RotateCcw size={16} />} onClick={onResetFilters}>
            Clear All Filters
          </Button>
        ) : undefined
      }
      className={className}
    />
  );
};
