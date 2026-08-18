import { FC } from 'react';
import { Pagination } from '../../../components/ui/Pagination';

export interface OrderPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

/** Zero-indexed pagination for the order history list (backend Spring Page). */
export const OrderPagination: FC<OrderPaginationProps> = ({ currentPage, totalPages, onPageChange }) => (
  <Pagination
    currentPage={currentPage}
    totalPages={totalPages}
    onPageChange={onPageChange}
    ariaLabel="Order history pagination"
  />
);