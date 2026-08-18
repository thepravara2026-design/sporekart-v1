import { forwardRef, InputHTMLAttributes } from 'react';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  invalid?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(({
  invalid = false,
  disabled = false,
  className = '',
  style = {},
  type = 'text',
  ...props
}, ref) => {
  return (
    <input
      ref={ref}
      type={type}
      disabled={disabled}
      aria-invalid={invalid ? true : undefined}
      className={`form-input ${invalid ? 'input-invalid' : ''} ${className}`}
      style={{
        width: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.3)',
        border: invalid ? '1px solid var(--danger-color)' : '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        padding: '0.75rem 1rem',
        color: 'var(--text-primary)',
        fontSize: '1rem',
        fontFamily: 'var(--font-family)',
        outline: 'none',
        opacity: disabled ? 0.5 : 1,
        cursor: disabled ? 'not-allowed' : 'text',
        transition: 'border-color var(--transition-fast)',
        ...style,
      }}
      {...props}
    />
  );
});

Input.displayName = 'Input';
