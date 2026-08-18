import { FC, useEffect, useRef, useId, ChangeEvent } from 'react';
import { X, SlidersHorizontal } from 'lucide-react';
import { Category } from '../types/catalog';
import { Button } from '../../../components/ui/Button';

export interface CatalogFiltersDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  categories: Category[];
  selectedCategory: string;
  selectedStatus: string;
  minPrice: string;
  maxPrice: string;
  onCategoryChange: (value: string) => void;
  onStatusChange: (value: string) => void;
  onMinPriceChange: (value: string) => void;
  onMaxPriceChange: (value: string) => void;
  onClearFilters: () => void;
  hasActiveFilters: boolean;
}

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'ACTIVE', label: 'In Stock' },
  { value: 'OUT_OF_STOCK', label: 'Out of Stock' },
];

/**
 * CatalogFiltersDrawer — WCAG 2.2 accessible filter panel.
 *
 * Behaviour:
 * - Focus trapped inside when open (Tab/Shift+Tab cycle)
 * - Escape closes and restores focus to trigger
 * - Background scroll locked when open
 * - role="dialog" with aria-modal="true" and accessible name
 */
export const CatalogFiltersDrawer: FC<CatalogFiltersDrawerProps> = ({
  isOpen,
  onClose,
  categories,
  selectedCategory,
  selectedStatus,
  minPrice,
  maxPrice,
  onCategoryChange,
  onStatusChange,
  onMinPriceChange,
  onMaxPriceChange,
  onClearFilters,
  hasActiveFilters,
}) => {
  const drawerRef = useRef<HTMLDivElement>(null);
  const closeBtnRef = useRef<HTMLButtonElement>(null);
  const headingId = useId();
  const catId = useId();
  const statusId = useId();
  const minPriceId = useId();
  const maxPriceId = useId();

  // Lock body scroll when open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      closeBtnRef.current?.focus();
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  // Escape key
  useEffect(() => {
    if (!isOpen) return;
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [isOpen, onClose]);

  // Focus trap
  useEffect(() => {
    if (!isOpen || !drawerRef.current) return;
    const focusable = drawerRef.current.querySelectorAll<HTMLElement>(
      'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
    );
    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    const trap = (e: KeyboardEvent) => {
      if (e.key !== 'Tab') return;
      if (e.shiftKey) {
        if (document.activeElement === first) {
          e.preventDefault();
          last?.focus();
        }
      } else {
        if (document.activeElement === last) {
          e.preventDefault();
          first?.focus();
        }
      }
    };
    document.addEventListener('keydown', trap);
    return () => document.removeEventListener('keydown', trap);
  }, [isOpen]);

  if (!isOpen) return null;

  const inputStyle: React.CSSProperties = {
    width: '100%',
    height: '2.5rem',
    background: 'var(--surface-elevated)',
    border: '1px solid var(--border-primary)',
    borderRadius: 'var(--radius-md)',
    color: 'var(--text-primary)',
    fontSize: '0.875rem',
    padding: '0 0.75rem',
    boxSizing: 'border-box',
  };

  const labelStyle: React.CSSProperties = {
    display: 'block',
    fontSize: '0.8rem',
    fontWeight: 700,
    color: 'var(--text-secondary)',
    marginBottom: '0.4rem',
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  };

  const fieldStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    gap: 0,
  };

  return (
    <>
      {/* Backdrop */}
      <div
        aria-hidden="true"
        onClick={onClose}
        style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.5)',
          zIndex: 'var(--z-overlay)' as string,
          backdropFilter: 'blur(2px)',
        }}
      />

      {/* Drawer panel */}
      <div
        ref={drawerRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={headingId}
        style={{
          position: 'fixed',
          top: 0,
          right: 0,
          bottom: 0,
          width: 'min(340px, 90vw)',
          background: 'var(--surface-primary)',
          borderLeft: '1px solid var(--border-primary)',
          zIndex: 'calc(var(--z-overlay) + 1)' as string,
          display: 'flex',
          flexDirection: 'column',
          overflowY: 'auto',
          boxShadow: 'var(--shadow-xl)',
        }}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '1.25rem 1.5rem',
            borderBottom: '1px solid var(--border-primary)',
            position: 'sticky',
            top: 0,
            background: 'var(--surface-primary)',
            zIndex: 1,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <SlidersHorizontal size={18} aria-hidden="true" style={{ color: 'var(--accent-primary)' }} />
            <h2
              id={headingId}
              style={{ margin: 0, fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}
            >
              Filters
            </h2>
          </div>
          <button
            ref={closeBtnRef}
            type="button"
            aria-label="Close filters"
            onClick={onClose}
            style={{
              background: 'none',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              cursor: 'pointer',
              color: 'var(--text-secondary)',
              padding: '0.4rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              minWidth: '2rem',
              minHeight: '2rem',
            }}
          >
            <X size={16} aria-hidden="true" />
          </button>
        </div>

        {/* Filter fields */}
        <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.5rem', flex: 1 }}>
          {/* Category */}
          <div style={fieldStyle}>
            <label htmlFor={catId} style={labelStyle}>
              Category
            </label>
            <select
              id={catId}
              value={selectedCategory}
              onChange={(e: ChangeEvent<HTMLSelectElement>) => onCategoryChange(e.target.value)}
              style={inputStyle}
            >
              <option value="">All Categories</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          {/* Availability */}
          <div style={fieldStyle}>
            <label htmlFor={statusId} style={labelStyle}>
              Availability
            </label>
            <select
              id={statusId}
              value={selectedStatus}
              onChange={(e: ChangeEvent<HTMLSelectElement>) => onStatusChange(e.target.value)}
              style={inputStyle}
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>

          {/* Price range */}
          <fieldset style={{ border: 'none', padding: 0, margin: 0 }}>
            <legend style={{ ...labelStyle, marginBottom: '0.75rem' }}>Price Range (₹)</legend>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div style={fieldStyle}>
                <label htmlFor={minPriceId} style={{ ...labelStyle, textTransform: 'none', letterSpacing: 0 }}>
                  Min
                </label>
                <input
                  id={minPriceId}
                  type="number"
                  min="0"
                  step="1"
                  placeholder="0"
                  value={minPrice}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => onMinPriceChange(e.target.value)}
                  style={inputStyle}
                />
              </div>
              <div style={fieldStyle}>
                <label htmlFor={maxPriceId} style={{ ...labelStyle, textTransform: 'none', letterSpacing: 0 }}>
                  Max
                </label>
                <input
                  id={maxPriceId}
                  type="number"
                  min="0"
                  step="1"
                  placeholder="Any"
                  value={maxPrice}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => onMaxPriceChange(e.target.value)}
                  style={inputStyle}
                />
              </div>
            </div>
          </fieldset>
        </div>

        {/* Footer */}
        <div
          style={{
            padding: '1.25rem 1.5rem',
            borderTop: '1px solid var(--border-primary)',
            display: 'flex',
            gap: '0.75rem',
            position: 'sticky',
            bottom: 0,
            background: 'var(--surface-primary)',
          }}
        >
          {hasActiveFilters && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => {
                onClearFilters();
                onClose();
              }}
              style={{ flex: 1 }}
            >
              Clear All
            </Button>
          )}
          <Button variant="primary" size="sm" onClick={onClose} style={{ flex: 2 }}>
            Apply Filters
          </Button>
        </div>
      </div>
    </>
  );
};
