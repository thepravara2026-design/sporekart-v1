import { FC, ReactNode, createContext, useContext, useState, useCallback } from 'react';
import { AlertCircle, CheckCircle, AlertTriangle, Info, X } from 'lucide-react';
import { zIndex } from '../../design-system/tokens';

export type ToastVariant = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: string;
  title?: string;
  message: ReactNode;
  variant?: ToastVariant;
  duration?: number;
}

interface ToastContextType {
  addToast: (toast: Omit<ToastMessage, 'id'>) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    return {
      addToast: () => {},
      removeToast: () => {},
    };
  }
  return context;
};

export const ToastProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    ({ message, title, variant = 'info', duration = 4000 }: Omit<ToastMessage, 'id'>) => {
      const id = `toast-${Math.random().toString(36).substring(2, 9)}`;
      const newToast: ToastMessage = { id, message, title, variant, duration };

      setToasts((prev) => [...prev, newToast]);

      if (duration > 0) {
        setTimeout(() => {
          removeToast(id);
        }, duration);
      }
    },
    [removeToast]
  );

  return (
    <ToastContext.Provider value={{ addToast, removeToast }}>
      {children}
      <div
        className="toast-container"
        style={{
          position: 'fixed',
          bottom: '1.5rem',
          right: '1.5rem',
          zIndex: zIndex.toast,
          display: 'flex',
          flexDirection: 'column',
          gap: '0.75rem',
          maxWidth: '380px',
          width: '100%',
          pointerEvents: 'none',
        }}
      >
        {toasts.map((toast) => {
          const getVariantIcon = () => {
            switch (toast.variant) {
              case 'success': return <CheckCircle size={20} style={{ color: '#10b981' }} />;
              case 'error': return <AlertCircle size={20} style={{ color: '#ef4444' }} />;
              case 'warning': return <AlertTriangle size={20} style={{ color: '#f59e0b' }} />;
              case 'info':
              default: return <Info size={20} style={{ color: '#3b82f6' }} />;
            }
          };

          return (
            <div
              key={toast.id}
              role={toast.variant === 'error' ? 'alert' : 'status'}
              aria-live={toast.variant === 'error' ? 'assertive' : 'polite'}
              style={{
                pointerEvents: 'auto',
                backgroundColor: 'var(--bg-surface)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-lg)',
                padding: '1rem 1.25rem',
                boxShadow: 'var(--shadow-xl)',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '0.75rem',
                color: 'var(--text-primary)',
              }}
            >
              <div style={{ marginTop: '0.1rem' }}>{getVariantIcon()}</div>
              <div style={{ flex: 1 }}>
                {toast.title && (
                  <div style={{ fontWeight: 700, fontSize: '0.9rem', marginBottom: '0.2rem' }}>
                    {toast.title}
                  </div>
                )}
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{toast.message}</div>
              </div>
              <button
                type="button"
                aria-label="Dismiss notification"
                onClick={() => removeToast(toast.id)}
                style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', padding: '0.2rem' }}
              >
                <X size={16} />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
};
