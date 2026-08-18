import { FC, KeyboardEvent } from 'react';

export interface SwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label?: string;
  description?: string;
  disabled?: boolean;
  id?: string;
  className?: string;
}

export const Switch: FC<SwitchProps> = ({
  checked,
  onChange,
  label,
  description,
  disabled = false,
  id,
  className = '',
}) => {
  const switchId = id || `switch-${Math.random().toString(36).substring(2, 9)}`;

  const handleToggle = () => {
    if (!disabled) {
      onChange(!checked);
    }
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLButtonElement>) => {
    if (e.key === ' ' || e.key === 'Enter') {
      e.preventDefault();
      handleToggle();
    }
  };

  return (
    <div className={`switch-container ${className}`} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
      <button
        type="button"
        role="switch"
        id={switchId}
        aria-checked={checked}
        aria-label={label || 'Toggle switch'}
        disabled={disabled}
        onClick={handleToggle}
        onKeyDown={handleKeyDown}
        style={{
          width: '44px',
          height: '24px',
          borderRadius: 'var(--radius-full)',
          backgroundColor: checked ? 'var(--accent-primary)' : 'rgba(255, 255, 255, 0.2)',
          position: 'relative',
          border: 'none',
          cursor: disabled ? 'not-allowed' : 'pointer',
          opacity: disabled ? 0.5 : 1,
          transition: 'background-color var(--transition-fast)',
          padding: '2px',
        }}
      >
        <span
          style={{
            display: 'block',
            width: '20px',
            height: '20px',
            borderRadius: '50%',
            backgroundColor: '#ffffff',
            transform: checked ? 'translateX(20px)' : 'translateX(0px)',
            transition: 'transform var(--transition-fast)',
            boxShadow: 'var(--shadow-sm)',
          }}
        />
      </button>
      {label && (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <label htmlFor={switchId} style={{ fontSize: '0.95rem', fontWeight: 500, cursor: disabled ? 'not-allowed' : 'pointer' }}>
            {label}
          </label>
          {description && (
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{description}</span>
          )}
        </div>
      )}
    </div>
  );
};
