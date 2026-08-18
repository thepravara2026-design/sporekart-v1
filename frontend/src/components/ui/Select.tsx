import { forwardRef, SelectHTMLAttributes, ReactNode } from 'react';

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  options?: SelectOption[];
  invalid?: boolean;
  children?: ReactNode;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(({
  options,
  children,
  invalid = false,
  disabled = false,
  className = '',
  style = {},
  ...props
}, ref) => {
  return (
    <select
      ref={ref}
      disabled={disabled}
      aria-invalid={invalid ? true : undefined}
      className={`form-select ${invalid ? 'input-invalid' : ''} ${className}`}
      style={{
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        border: invalid ? '1px solid var(--danger-color)' : '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        padding: '0.75rem 1rem',
        color: 'var(--text-primary)',
        fontSize: '1rem',
        fontFamily: 'var(--font-family)',
        outline: 'none',
        opacity: disabled ? 0.5 : 1,
        cursor: disabled ? 'not-allowed' : 'pointer',
        ...style,
      }}
      {...props}
    >
      {options
        ? options.map((opt) => (
            <option key={opt.value} value={opt.value} disabled={opt.disabled} style={{ background: '#0a291d', color: '#fff' }}>
              {opt.label}
            </option>
          ))
        : children}
    </select>
  );
});

Select.displayName = 'Select';
