import { FC, ReactNode } from 'react';
import { PackageOpen } from 'lucide-react';

export interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

export const EmptyState: FC<EmptyStateProps> = ({
  icon = <PackageOpen size={48} style={{ color: 'var(--accent-primary)' }} />,
  title,
  description,
  action,
  className = '',
  style = {},
}) => {
  return (
    <div
      className={`empty-state card ${className}`}
      style={{
        textAlign: 'center',
        padding: '3rem 2rem',
        alignItems: 'center',
        justifyContent: 'center',
        ...style,
      }}
    >
      <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'center' }}>{icon}</div>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
        {title}
      </h3>
      {description && (
        <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', maxWidth: '420px', margin: '0 auto 1.5rem auto' }}>
          {description}
        </p>
      )}
      {action && <div style={{ display: 'flex', justifyContent: 'center' }}>{action}</div>}
    </div>
  );
};
