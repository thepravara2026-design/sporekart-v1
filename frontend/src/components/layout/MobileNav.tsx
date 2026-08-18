import { FC, useEffect, useRef } from 'react';
import { NavLink } from 'react-router-dom';
import { X, Sprout } from 'lucide-react';
import { NavItem } from '../../config/navigation';

interface MobileNavProps {
  isOpen: boolean;
  onClose: () => void;
  items: NavItem[];
  triggerRef?: React.RefObject<HTMLButtonElement | null>;
}

export const MobileNav: FC<MobileNavProps> = ({ isOpen, onClose, items, triggerRef }) => {
  const drawerRef = useRef<HTMLDivElement>(null);

  // Close on Escape key press & Focus Management
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
        triggerRef?.current?.focus();
      }
    };

    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
      // Focus close button on open
      const closeButton = drawerRef.current?.querySelector<HTMLButtonElement>('.mobile-nav-close');
      closeButton?.focus();
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose, triggerRef]);

  if (!isOpen) return null;

  return (
    <div
      className="mobile-nav-overlay"
      role="dialog"
      aria-modal="true"
      aria-label="Mobile Navigation Menu"
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(5, 28, 20, 0.85)',
        backdropFilter: 'blur(8px)',
        zIndex: 1000,
        display: 'flex',
        justifyContent: 'flex-end',
      }}
      onClick={onClose}
    >
      <div
        ref={drawerRef}
        className="mobile-nav-drawer"
        style={{
          width: '80%',
          maxWidth: '320px',
          height: '100%',
          backgroundColor: 'var(--bg-surface)',
          borderLeft: '1px solid var(--border-color)',
          padding: '1.5rem',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: 'var(--shadow-xl)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 800, color: 'var(--accent-primary)' }}>
            <Sprout size={24} />
            <span>SPOREKART</span>
          </div>
          <button
            className="mobile-nav-close btn btn-secondary btn-sm"
            onClick={() => {
              onClose();
              triggerRef?.current?.focus();
            }}
            aria-label="Close mobile navigation"
            style={{ padding: '0.5rem' }}
          >
            <X size={20} />
          </button>
        </div>

        <nav aria-label="Mobile primary navigation" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {items.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
              style={{
                fontSize: '1.125rem',
                padding: '0.75rem 1rem',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                display: 'block',
              }}
              onClick={onClose}
              end={item.path === '/'}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
};
