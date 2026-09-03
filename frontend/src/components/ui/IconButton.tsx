import { forwardRef, ButtonHTMLAttributes, ReactNode } from 'react';
import { ButtonVariant, ButtonSize } from './Button';

export interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  icon: ReactNode;
  'aria-label': string; // Mandatory accessible name
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
}

export const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>(({
  icon,
  'aria-label': ariaLabel,
  variant = 'secondary',
  size = 'md',
  isLoading = false,
  disabled = false,
  className = '',
  style = {},
  type = 'button',
  ...props
}, ref) => {
  const getSizeDimensions = (): string => {
    switch (size) {
      case 'sm': return '32px';
      case 'lg': return '48px';
      case 'md':
      default: return '40px';
    }
  };

  const dimensions = getSizeDimensions();

  return (
    <button
      ref={ref}
      type={type}
      aria-label={ariaLabel}
      disabled={disabled || isLoading}
      className={`icon-button btn btn-${variant} ${className}`}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: dimensions,
        height: dimensions,
        padding: 0,
        borderRadius: 'var(--radius-md)',
        backgroundColor: variant === 'secondary' ? 'rgba(255, 255, 255, 0.08)' : undefined,
        color: 'var(--text-primary)',
        border: '1px solid var(--border-color)',
        cursor: (disabled || isLoading) ? 'not-allowed' : 'pointer',
        opacity: (disabled || isLoading) ? 0.55 : 1,
        transition: 'all var(--transition-fast)',
        ...style,
      }}
      {...props}
    >
      {icon}
    </button>
  );
});

IconButton.displayName = 'IconButton';
