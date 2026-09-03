import { FC, ReactNode } from 'react';

export type BadgeVariant = 'default' | 'neutral' | 'success' | 'warning' | 'danger' | 'info';

export interface BadgeProps {
  children: ReactNode;
  variant?: BadgeVariant;
  icon?: ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

export const Badge: FC<BadgeProps> = ({
  children,
  variant = 'default',
  icon,
  className = '',
  style = {},
}) => {
  const getVariantStyles = (): React.CSSProperties => {
    switch (variant) {
      case 'success':
        return { backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' };
      case 'warning':
        return { backgroundColor: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b', border: '1px solid rgba(245, 158, 11, 0.3)' };
      case 'danger':
        return { backgroundColor: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' };
      case 'info':
        return { backgroundColor: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.3)' };
      case 'neutral':
        return { backgroundColor: 'rgba(255, 255, 255, 0.08)', color: 'var(--text-secondary)', border: '1px solid var(--border-color)' };
      case 'default':
      default:
        return { backgroundColor: 'rgba(16, 185, 129, 0.2)', color: 'var(--accent-primary)', border: '1px solid var(--accent-primary)' };
    }
  };

  return (
    <span
      className={`badge badge-${variant} ${className}`}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.35rem',
        padding: '0.25rem 0.75rem',
        borderRadius: 'var(--radius-full)',
        fontSize: '0.85rem',
        fontWeight: 600,
        ...getVariantStyles(),
        ...style,
      }}
    >
      {icon && <span className="badge-icon">{icon}</span>}
      {children}
    </span>
  );
};
