import { forwardRef, ButtonHTMLAttributes, ReactNode } from 'react';
import { LoadingSpinner } from './LoadingSpinner';

export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'destructive' | 'link';
export type ButtonSize = 'sm' | 'md' | 'lg';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  fullWidth?: boolean;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  fullWidth = false,
  disabled = false,
  leftIcon,
  rightIcon,
  className = '',
  style = {},
  type = 'button',
  ...props
}, ref) => {
  const getVariantStyles = (): React.CSSProperties => {
    switch (variant) {
      case 'secondary':
        return {
          backgroundColor: 'rgba(255, 255, 255, 0.08)',
          color: 'var(--text-primary)',
          border: '1px solid var(--border-color)',
        };
      case 'outline':
        return {
          backgroundColor: 'transparent',
          color: 'var(--accent-primary)',
          border: '1px solid var(--accent-primary)',
        };
      case 'ghost':
        return {
          backgroundColor: 'transparent',
          color: 'var(--text-primary)',
          border: '1px solid transparent',
        };
      case 'destructive':
        return {
          backgroundColor: 'var(--danger-color)',
          color: '#ffffff',
          border: 'none',
        };
      case 'link':
        return {
          backgroundColor: 'transparent',
          color: 'var(--accent-primary)',
          border: 'none',
          padding: 0,
          textDecoration: 'underline',
        };
      case 'primary':
      default:
        return {
          background: 'linear-gradient(135deg, #10b981, #059669)',
          color: '#ffffff',
          border: 'none',
        };
    }
  };

  const getSizeStyles = (): React.CSSProperties => {
    if (variant === 'link') return {};
    switch (size) {
      case 'sm':
        return { padding: '0.4rem 0.85rem', fontSize: '0.875rem' };
      case 'lg':
        return { padding: '0.85rem 1.85rem', fontSize: '1.125rem' };
      case 'md':
      default:
        return { padding: '0.75rem 1.5rem', fontSize: '1rem' };
    }
  };

  const isDisabled = disabled || isLoading;

  return (
    <button
      ref={ref}
      type={type}
      disabled={isDisabled}
      aria-busy={isLoading ? true : undefined}
      className={`btn btn-${variant} ${className}`}
      style={{
        display: fullWidth ? 'flex' : 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '0.5rem',
        width: fullWidth ? '100%' : 'auto',
        borderRadius: 'var(--radius-md)',
        fontWeight: 600,
        cursor: isDisabled ? 'not-allowed' : 'pointer',
        opacity: isDisabled ? 0.55 : 1,
        transition: 'all var(--transition-fast)',
        fontFamily: 'var(--font-family)',
        ...getVariantStyles(),
        ...getSizeStyles(),
        ...style,
      }}
      {...props}
    >
      {isLoading ? (
        <LoadingSpinner size={16} label="Loading..." className="button-spinner" />
      ) : (
        <>
          {leftIcon && <span className="btn-icon-left">{leftIcon}</span>}
          {children}
          {rightIcon && <span className="btn-icon-right">{rightIcon}</span>}
        </>
      )}
    </button>
  );
});

Button.displayName = 'Button';
