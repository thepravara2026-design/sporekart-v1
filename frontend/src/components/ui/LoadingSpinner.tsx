import { FC } from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  size?: number;
  label?: string;
  className?: string;
}

export const LoadingSpinner: FC<LoadingSpinnerProps> = ({
  size = 32,
  label = 'Loading content...',
  className = '',
}) => {
  return (
    <div
      role="status"
      aria-label={label}
      className={`loading-spinner-container ${className}`}
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem',
        gap: '0.75rem',
      }}
    >
      <Loader2
        size={size}
        style={{
          color: 'var(--accent-primary)',
          animation: 'spin 1s linear infinite',
        }}
      />
      <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{label}</span>
    </div>
  );
};
