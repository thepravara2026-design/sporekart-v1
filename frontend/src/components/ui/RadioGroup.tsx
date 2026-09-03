import { FC, ReactNode, ChangeEvent } from 'react';

export interface RadioOption {
  value: string;
  label: ReactNode;
  description?: string;
  disabled?: boolean;
}

export interface RadioGroupProps {
  name: string;
  options: RadioOption[];
  value?: string;
  onChange?: (value: string) => void;
  label?: string;
  disabled?: boolean;
  invalid?: boolean;
  className?: string;
}

export const RadioGroup: FC<RadioGroupProps> = ({
  name,
  options,
  value,
  onChange,
  label,
  disabled = false,
  invalid = false,
  className = '',
}) => {
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    onChange?.(e.target.value);
  };

  return (
    <fieldset
      className={`radio-group ${className}`}
      style={{ border: 'none', padding: 0, margin: 0 }}
      aria-invalid={invalid ? true : undefined}
    >
      {label && (
        <legend style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
          {label}
        </legend>
      )}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {options.map((opt) => {
          const optionId = `${name}-${opt.value}`;
          const isOptionDisabled = disabled || opt.disabled;
          return (
            <div key={opt.value} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
              <input
                type="radio"
                id={optionId}
                name={name}
                value={opt.value}
                checked={value === opt.value}
                disabled={isOptionDisabled}
                onChange={handleChange}
                style={{
                  width: '18px',
                  height: '18px',
                  accentColor: 'var(--accent-primary)',
                  cursor: isOptionDisabled ? 'not-allowed' : 'pointer',
                  marginTop: '0.15rem',
                }}
              />
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <label
                  htmlFor={optionId}
                  style={{
                    fontSize: '0.95rem',
                    fontWeight: 500,
                    color: 'var(--text-primary)',
                    cursor: isOptionDisabled ? 'not-allowed' : 'pointer',
                  }}
                >
                  {opt.label}
                </label>
                {opt.description && (
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{opt.description}</span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </fieldset>
  );
};
