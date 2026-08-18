import { FC } from 'react';
import { Pagination } from '../../../components/ui/Pagination';

export interface CatalogPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  className?: string;
}

export const CatalogPagination: FC<CatalogPaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  className = '',
}) => {
  return (
    <Pagination
      currentPage={currentPage}
      totalPages={totalPages}
      onPageChange={onPageChange}
      ariaLabel="Catalog page navigation"
      className={className}
    />
  );
};
