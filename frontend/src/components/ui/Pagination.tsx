import { FC } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  ariaLabel?: string;
  className?: string;
}

export const Pagination: FC<PaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  ariaLabel = 'Pagination navigation',
  className = '',
}) => {
  if (totalPages <= 1) return null;

  const pages: number[] = [];
  for (let i = 0; i < totalPages; i++) {
    pages.push(i);
  }

  return (
    <nav aria-label={ariaLabel} className={`pagination-container ${className}`} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginTop: '1.5rem' }}>
      <button
        type="button"
        className="btn btn-secondary btn-sm"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
        aria-label="Go to previous page"
        style={{ padding: '0.4rem 0.65rem' }}
      >
        <ChevronLeft size={16} />
      </button>

      {pages.map((p) => {
        const isCurrent = p === currentPage;
        return (
          <button
            key={p}
            type="button"
            className={`btn ${isCurrent ? 'btn-primary' : 'btn-secondary'} btn-sm`}
            aria-current={isCurrent ? 'page' : undefined}
            aria-label={`Go to page ${p + 1}`}
            onClick={() => onPageChange(p)}
            style={{ padding: '0.4rem 0.75rem', minWidth: '32px' }}
          >
            {p + 1}
          </button>
        );
      })}

      <button
        type="button"
        className="btn btn-secondary btn-sm"
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
        aria-label="Go to next page"
        style={{ padding: '0.4rem 0.65rem' }}
      >
        <ChevronRight size={16} />
      </button>
    </nav>
  );
};
