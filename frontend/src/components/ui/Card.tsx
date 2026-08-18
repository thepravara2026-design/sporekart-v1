import { FC, ReactNode, HTMLAttributes, CSSProperties, forwardRef } from 'react';

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode;
}

export const Card = forwardRef<HTMLDivElement, CardProps>(({ children, className = '', style = {}, ...props }, ref) => (
  <div
    ref={ref}
    className={`card ${className}`}
    style={{
      background: 'var(--bg-card)',
      backdropFilter: 'blur(8px)',
      border: '1px solid var(--border-color)',
      borderRadius: 'var(--radius-lg)',
      padding: '1.5rem',
      boxShadow: 'var(--shadow-lg)',
      display: 'flex',
      flexDirection: 'column',
      gap: '1rem',
      ...style,
    }}
    {...props}
  >
    {children}
  </div>
));

Card.displayName = 'Card';

export const CardHeader: FC<CardProps> = ({ children, className = '', style = {}, ...props }) => (
  <div className={`card-header ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', ...style }} {...props}>
    {children}
  </div>
);

export const CardTitle: FC<{ children: ReactNode; className?: string; style?: CSSProperties }> = ({ children, className = '', style = {} }) => (
  <h3 className={`card-title ${className}`} style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', margin: 0, ...style }}>
    {children}
  </h3>
);

export const CardDescription: FC<{ children: ReactNode; className?: string; style?: CSSProperties }> = ({ children, className = '', style = {} }) => (
  <p className={`card-description ${className}`} style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: 0, ...style }}>
    {children}
  </p>
);

export const CardContent: FC<CardProps> = ({ children, className = '', style = {}, ...props }) => (
  <div className={`card-content ${className}`} style={{ flex: 1, ...style }} {...props}>
    {children}
  </div>
);

export const CardFooter: FC<CardProps> = ({ children, className = '', style = {}, ...props }) => (
  <div className={`card-footer ${className}`} style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem', ...style }} {...props}>
    {children}
  </div>
);
