import { FC, ReactNode } from 'react';
import { Container } from './Container';

interface PageShellProps {
  children: ReactNode;
  title?: string;
  subtitle?: string;
  actions?: ReactNode;
  breadcrumbs?: ReactNode;
}

export const PageShell: FC<PageShellProps> = ({
  children,
  title,
  subtitle,
  actions,
  breadcrumbs,
}) => {
  return (
    <Container maxWidth="xl" style={{ paddingTop: '2rem', paddingBottom: '3rem' }}>
      {breadcrumbs && <nav aria-label="Breadcrumb navigation" style={{ marginBottom: '1rem' }}>{breadcrumbs}</nav>}

      {(title || actions) && (
        <header className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
          <div>
            {title && <h1 style={{ fontSize: '2.25rem', fontWeight: 800 }}>{title}</h1>}
            {subtitle && <p className="page-subtitle">{subtitle}</p>}
          </div>
          {actions && <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>{actions}</div>}
        </header>
      )}

      {children}
    </Container>
  );
};
