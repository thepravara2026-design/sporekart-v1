import { forwardRef, TextareaHTMLAttributes } from 'react';

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  invalid?: boolean;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(({
  invalid = false,
  disabled = false,
  rows = 4,
  className = '',
  style = {},
  ...props
}, ref) => {
  return (
    <textarea
      ref={ref}
      rows={rows}
      disabled={disabled}
      aria-invalid={invalid ? true : undefined}
      className={`form-textarea ${invalid ? 'input-invalid' : ''} ${className}`}
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
        resize: 'vertical',
        opacity: disabled ? 0.5 : 1,
        cursor: disabled ? 'not-allowed' : 'text',
        transition: 'border-color var(--transition-fast)',
        ...style,
      }}
      {...props}
    />
  );
});

Textarea.displayName = 'Textarea';
