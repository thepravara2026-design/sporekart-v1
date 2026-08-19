import { FC, useState, useEffect, FormEvent, useRef, useId } from 'react';
import { Search, X, Loader2 } from 'lucide-react';

export interface CatalogSearchProps {
  /** The committed search value (from URL / parent state). */
  value: string;
  /** Called when the user submits or clears. */
  onSearch: (value: string) => void;
  placeholder?: string;
  isLoading?: boolean;
  className?: string;
}

/**
 * CatalogSearch — accessible, URL-synced, submit-on-Enter search control.
 *
 * Design decisions:
 * - Local `term` state mirrors `value` prop; syncs when prop changes (e.g. browser Back).
 * - Search is submitted explicitly (form submit / Enter) to avoid per-keystroke API calls.
 * - Clear button both clears local term AND calls onSearch('') immediately.
 * - Loading indicator shown when isLoading=true and a search is active.
 */
export const CatalogSearch: FC<CatalogSearchProps> = ({
  value,
  onSearch,
  placeholder = 'Search spawn, cultures, substrate...',
  isLoading = false,
  className = '',
}) => {
  const inputId = useId();
  const [term, setTerm] = useState(value);
  const inputRef = useRef<HTMLInputElement>(null);

  // Sync local term when the URL-driven value changes (e.g. browser Back/Forward)
  useEffect(() => {
    setTerm(value);
  }, [value]);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSearch(term.trim());
  };

  const handleClear = () => {
    setTerm('');
    onSearch('');
    inputRef.current?.focus();
  };

  return (
    <form
      onSubmit={handleSubmit}
      className={`catalog-search-form ${className}`}
      role="search"
      aria-label="Search products"
      style={{ display: 'flex', gap: '0.5rem', width: '100%' }}
    >
      <label htmlFor={inputId} className="sr-only">
        Search products
      </label>
      <div style={{ position: 'relative', flex: 1 }}>
        {/* search icon — left */}
        <Search
          size={16}
          aria-hidden="true"
          style={{
            position: 'absolute',
            left: '0.75rem',
            top: '50%',
            transform: 'translateY(-50%)',
            color: 'var(--text-secondary)',
            pointerEvents: 'none',
          }}
        />
        <input
          ref={inputRef}
          id={inputId}
          type="search"
          value={term}
          onChange={(e) => setTerm(e.target.value)}
          placeholder={placeholder}
          autoComplete="off"
          spellCheck={false}
          style={{
            width: '100%',
            paddingLeft: '2.25rem',
            paddingRight: term ? '2.5rem' : '1rem',
            height: '2.5rem',
            background: 'var(--surface-elevated)',
            border: '1px solid var(--border-primary)',
            borderRadius: 'var(--radius-md)',
            color: 'var(--text-primary)',
            fontSize: '0.9rem',
            outline: 'none',
            boxSizing: 'border-box',
            transition: 'border-color var(--transition-fast), box-shadow var(--transition-fast)',
            boxShadow: '0 0 0 0 transparent',
          }}
          onFocus={(e) => {
            e.currentTarget.style.borderColor = 'var(--accent-primary)';
            e.currentTarget.style.boxShadow = '0 0 0 3px rgba(16, 185, 129, 0.18), 0 0 12px 0 rgba(16, 185, 129, 0.25)';
          }}
          onBlur={(e) => {
            e.currentTarget.style.borderColor = 'var(--border-primary)';
            e.currentTarget.style.boxShadow = '0 0 0 0 transparent';
          }}
        />
        {/* loading spinner when search active */}
        {isLoading && term && (
          <Loader2
            size={14}
            aria-label="Searching..."
            style={{
              position: 'absolute',
              right: '0.75rem',
              top: '50%',
              transform: 'translateY(-50%)',
              color: 'var(--accent-primary)',
              animation: 'spin 1s linear infinite',
            }}
          />
        )}
        {/* clear button — only when not loading */}
        {term && !isLoading && (
          <button
            type="button"
            aria-label="Clear search"
            onClick={handleClear}
            style={{
              position: 'absolute',
              right: '0.5rem',
              top: '50%',
              transform: 'translateY(-50%)',
              background: 'none',
              border: 'none',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              padding: '0.25rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              minWidth: '24px',
              minHeight: '24px',
              borderRadius: 'var(--radius-sm)',
            }}
          >
            <X size={14} aria-hidden="true" />
          </button>
        )}
      </div>
      <button
        type="submit"
        className="btn btn-primary"
        aria-label="Submit search"
        style={{ minWidth: '2.5rem', minHeight: '2.5rem', padding: '0 0.875rem' }}
      >
        <Search size={16} aria-hidden="true" />
        <span className="sr-only">Search</span>
      </button>
    </form>
  );
};
