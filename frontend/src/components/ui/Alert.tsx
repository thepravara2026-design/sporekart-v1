import { FC, ReactNode } from 'react';
import { AlertCircle, CheckCircle, AlertTriangle, Info, X } from 'lucide-react';

export type AlertVariant = 'info' | 'success' | 'warning' | 'error';

export interface AlertProps {
  title?: string;
  children: ReactNode;
  variant?: AlertVariant;
  onDismiss?: () => void;
  className?: string;
  style?: React.CSSProperties;
}

export const Alert: FC<AlertProps> = ({
  title,
  children,
  variant = 'info',
  onDismiss,
  className = '',
  style = {},
}) => {
  const getVariantDetails = () => {
    switch (variant) {
      case 'success':
        return {
          icon: <CheckCircle size={20} style={{ color: '#10b981' }} />,
          bg: 'rgba(16, 185, 129, 0.12)',
          border: '1px solid rgba(16, 185, 129, 0.3)',
          color: '#f9fafb',
        };
      case 'warning':
        return {
          icon: <AlertTriangle size={20} style={{ color: '#f59e0b' }} />,
          bg: 'rgba(245, 158, 11, 0.12)',
          border: '1px solid rgba(245, 158, 11, 0.3)',
          color: '#f9fafb',
        };
      case 'error':
        return {
          icon: <AlertCircle size={20} style={{ color: '#ef4444' }} />,
          bg: 'rgba(239, 68, 68, 0.12)',
          border: '1px solid rgba(239, 68, 68, 0.3)',
          color: '#f9fafb',
        };
      case 'info':
      default:
        return {
          icon: <Info size={20} style={{ color: '#3b82f6' }} />,
          bg: 'rgba(59, 130, 246, 0.12)',
          border: '1px solid rgba(59, 130, 246, 0.3)',
          color: '#f9fafb',
        };
    }
  };

  const details = getVariantDetails();

  return (
    <div
      role={variant === 'error' ? 'alert' : 'status'}
      aria-live={variant === 'error' ? 'assertive' : 'polite'}
      className={`alert alert-${variant} ${className}`}
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: '0.75rem',
        padding: '1rem 1.25rem',
        borderRadius: 'var(--radius-md)',
        backgroundColor: details.bg,
        border: details.border,
        color: details.color,
        width: '100%',
        ...style,
      }}
    >
      <div style={{ marginTop: '0.1rem' }}>{details.icon}</div>
      <div style={{ flex: 1 }}>
        {title && <div style={{ fontWeight: 700, fontSize: '0.95rem', marginBottom: '0.25rem' }}>{title}</div>}
        <div style={{ fontSize: '0.875rem', lineHeight: 1.5 }}>{children}</div>
      </div>
      {onDismiss && (
        <button
          type="button"
          aria-label="Dismiss alert"
          onClick={onDismiss}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--text-secondary)',
            cursor: 'pointer',
            padding: '0.2rem',
          }}
        >
          <X size={18} />
        </button>
      )}
    </div>
  );
};
