import { FC, ReactNode } from 'react';

export interface FormFieldProps {
  label?: string;
  description?: string;
  error?: string;
  required?: boolean;
  htmlFor?: string;
  children: ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

export const FormField: FC<FormFieldProps> = ({
  label,
  description,
  error,
  required = false,
  htmlFor,
  children,
  className = '',
  style = {},
}) => {
  const descId = htmlFor ? `${htmlFor}-desc` : undefined;
  const errorId = htmlFor ? `${htmlFor}-error` : undefined;

  return (
    <div className={`form-field ${className}`} style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', width: '100%', ...style }}>
      {label && (
        <label
          htmlFor={htmlFor}
          style={{
            fontSize: '0.875rem',
            fontWeight: 600,
            color: 'var(--text-primary)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.25rem',
          }}
        >
          {label}
          {required && <span style={{ color: 'var(--danger-color)' }}>*</span>}
        </label>
      )}

      {children}

      {description && !error && (
        <span id={descId} style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          {description}
        </span>
      )}

      {error && (
        <span id={errorId} role="alert" style={{ fontSize: '0.8rem', color: 'var(--danger-color)', fontWeight: 500 }}>
          {error}
        </span>
      )}
    </div>
  );
};
