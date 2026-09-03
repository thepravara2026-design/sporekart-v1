import { FC, ReactNode, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

export interface DrawerProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  position?: 'left' | 'right';
  triggerRef?: React.RefObject<HTMLElement | null>;
  className?: string;
}

export const Drawer: FC<DrawerProps> = ({
  isOpen,
  onClose,
  title,
  children,
  position = 'right',
  triggerRef,
  className = '',
}) => {
  const drawerRef = useRef<HTMLDivElement>(null);

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
      const closeBtn = drawerRef.current?.querySelector<HTMLButtonElement>('.drawer-close-btn');
      closeBtn?.focus();
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose, triggerRef]);

  if (!isOpen) return null;

  return createPortal(
    <div
      className="drawer-overlay"
      role="presentation"
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(5, 28, 20, 0.85)',
        backdropFilter: 'blur(8px)',
        zIndex: 1000,
        display: 'flex',
        justifyContent: position === 'left' ? 'flex-start' : 'flex-end',
      }}
      onClick={() => {
        onClose();
        triggerRef?.current?.focus();
      }}
    >
      <div
        ref={drawerRef}
        role="dialog"
        aria-modal="true"
        aria-label={title || 'Off-canvas panel'}
        className={`drawer-panel ${className}`}
        style={{
          width: '85%',
          maxWidth: '380px',
          height: '100%',
          backgroundColor: 'var(--bg-surface)',
          borderLeft: position === 'right' ? '1px solid var(--border-color)' : 'none',
          borderRight: position === 'left' ? '1px solid var(--border-color)' : 'none',
          padding: '1.75rem',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: 'var(--shadow-xl)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          {title && <h2 style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>{title}</h2>}
          <button
            type="button"
            className="drawer-close-btn btn btn-secondary btn-sm"
            aria-label="Close drawer panel"
            onClick={() => {
              onClose();
              triggerRef?.current?.focus();
            }}
            style={{ padding: '0.4rem' }}
          >
            <X size={18} />
          </button>
        </div>

        <div style={{ flex: 1, overflowY: 'auto' }}>{children}</div>
      </div>
    </div>,
    document.body
  );
};
