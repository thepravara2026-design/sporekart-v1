import { FC } from 'react';

interface PaginationControlsProps {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  isFirst: boolean;
  isLast: boolean;
  onPageChange: (page: number) => void;
}

export const PaginationControls: FC<PaginationControlsProps> = ({
  currentPage,
  totalPages,
  totalElements,
  isFirst,
  isLast,
  onPageChange,
}) => {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="pagination-container" data-testid="pagination-controls">
      <div className="pagination-info">
        Showing page <strong>{currentPage + 1}</strong> of <strong>{totalPages}</strong> ({totalElements} items total)
      </div>

      <div className="pagination-buttons">
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          disabled={isFirst || currentPage === 0}
          onClick={() => onPageChange(currentPage - 1)}
          aria-label="Previous Page"
        >
          &laquo; Previous
        </button>

        <span className="pagination-page-number">
          {currentPage + 1} / {totalPages}
        </span>

        <button
          type="button"
          className="btn btn-secondary btn-sm"
          disabled={isLast || currentPage >= totalPages - 1}
          onClick={() => onPageChange(currentPage + 1)}
          aria-label="Next Page"
        >
          Next &raquo;
        </button>
      </div>
    </div>
  );
};
