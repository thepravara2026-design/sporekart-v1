import { FC } from 'react';

interface SkipLinkProps {
  targetId?: string;
  label?: string;
}

export const SkipLink: FC<SkipLinkProps> = ({
  targetId = 'main-content',
  label = 'Skip to main content',
}) => {
  return (
    <a
      href={`#${targetId}`}
      className="skip-link"
      style={{
        position: 'absolute',
        top: '-9999px',
        left: '1rem',
        zIndex: 9999,
        padding: '0.75rem 1.25rem',
        background: 'var(--accent-primary)',
        color: '#ffffff',
        fontWeight: 700,
        borderRadius: 'var(--radius-md)',
        textDecoration: 'none',
        boxShadow: 'var(--shadow-glow)',
      }}
      onFocus={(e) => {
        e.currentTarget.style.top = '1rem';
      }}
      onBlur={(e) => {
        e.currentTarget.style.top = '-9999px';
      }}
    >
      {label}
    </a>
  );
};
