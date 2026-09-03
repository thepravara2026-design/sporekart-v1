import { FC, ReactNode } from 'react';

interface PageSectionProps {
  children: ReactNode;
  title?: string;
  subtitle?: string;
  ariaLabel?: string;
  className?: string;
  style?: React.CSSProperties;
}

export const PageSection: FC<PageSectionProps> = ({
  children,
  title,
  subtitle,
  ariaLabel,
  className = '',
  style = {},
}) => {
  return (
    <section
      aria-label={ariaLabel || title}
      className={`page-section ${className}`}
      style={{ marginBottom: '2.5rem', ...style }}
    >
      {title && (
        <div style={{ marginBottom: '1.25rem' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)' }}>{title}</h2>
          {subtitle && <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>{subtitle}</p>}
        </div>
      )}
      {children}
    </section>
  );
};
