import { FC, ReactNode } from 'react';
import { Card } from '../../../components/ui/Card';

export interface GrowerMetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: ReactNode;
  statusVariant?: 'success' | 'warning' | 'danger' | 'info' | 'neutral';
  onClick?: () => void;
}

export const GrowerMetricCard: FC<GrowerMetricCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  statusVariant = 'neutral',
  onClick,
}) => {
  const getBorderColor = () => {
    switch (statusVariant) {
      case 'success':
        return '#10b981';
      case 'warning':
        return '#f59e0b';
      case 'danger':
        return '#ef4444';
      case 'info':
        return '#3b82f6';
      default:
        return 'rgba(255, 255, 255, 0.1)';
    }
  };

  return (
    <Card
      style={{
        padding: '1.25rem',
        borderRadius: 'var(--radius-lg)',
        backgroundColor: 'var(--bg-card)',
        border: `1px solid ${getBorderColor()}`,
        cursor: onClick ? 'pointer' : 'default',
        boxShadow: 'var(--shadow-md)',
        transition: 'transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = 'var(--shadow-card-hover)';
        e.currentTarget.style.backgroundColor = 'var(--bg-card-hover)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = 'var(--shadow-md)';
        e.currentTarget.style.backgroundColor = 'var(--bg-card)';
      }}
      onClick={onClick}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <span style={{ fontSize: '0.875rem', fontWeight: 500, color: '#9ca3af', display: 'block', marginBottom: '0.25rem' }}>
            {title}
          </span>
          <span style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', lineHeight: 1.2 }}>
            {value}
          </span>
          {subtitle && (
            <span style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.375rem', display: 'block' }}>
              {subtitle}
            </span>
          )}
        </div>
        {icon && (
          <div
            style={{
              padding: '0.625rem',
              borderRadius: '0.375rem',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              color: '#10b981',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
};
