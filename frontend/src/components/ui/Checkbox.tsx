import { forwardRef, InputHTMLAttributes, ReactNode } from 'react';

export interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: ReactNode;
  description?: string;
  invalid?: boolean;
}

export const Checkbox = forwardRef<HTMLInputElement, CheckboxProps>(({
  label,
  description,
  invalid = false,
  disabled = false,
  className = '',
  id,
  style = {},
  ...props
}, ref) => {
  const checkboxId = id || `checkbox-${Math.random().toString(36).substring(2, 9)}`;

  return (
    <div className={`checkbox-container ${className}`} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', ...style }}>
      <input
        ref={ref}
        type="checkbox"
        id={checkboxId}
        disabled={disabled}
        aria-invalid={invalid ? true : undefined}
        style={{
          width: '18px',
          height: '18px',
          accentColor: 'var(--accent-primary)',
          cursor: disabled ? 'not-allowed' : 'pointer',
          marginTop: '0.15rem',
        }}
        {...props}
      />
      {label && (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <label
            htmlFor={checkboxId}
            style={{
              fontSize: '0.95rem',
              fontWeight: 500,
              color: 'var(--text-primary)',
              cursor: disabled ? 'not-allowed' : 'pointer',
            }}
          >
            {label}
          </label>
          {description && (
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
              {description}
            </span>
          )}
        </div>
      )}
    </div>
  );
});

Checkbox.displayName = 'Checkbox';
