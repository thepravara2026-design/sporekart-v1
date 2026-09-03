import { FC, ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

export interface BreadcrumbProps {
  items: BreadcrumbItem[];
  separator?: ReactNode;
  ariaLabel?: string;
  className?: string;
}

export const Breadcrumb: FC<BreadcrumbProps> = ({
  items,
  separator = <ChevronRight size={14} style={{ color: 'var(--text-muted)' }} />,
  ariaLabel = 'Breadcrumb navigation',
  className = '',
}) => {
  return (
    <nav aria-label={ariaLabel} className={`breadcrumb-nav ${className}`}>
      <ol style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', listStyle: 'none', padding: 0, margin: 0 }}>
        {items.map((item, index) => {
          const isLast = index === items.length - 1;
          return (
            <li key={index} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
              {index > 0 && <span aria-hidden="true">{separator}</span>}
              {isLast || !item.path ? (
                <span aria-current="page" style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {item.label}
                </span>
              ) : (
                <Link to={item.path} className="nav-link" style={{ color: 'var(--text-secondary)' }}>
                  {item.label}
                </Link>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};
