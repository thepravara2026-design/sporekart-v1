import { FC, useState, FormEvent } from 'react';
import { Input } from '../../../components/ui/Input';
import { Search, X } from 'lucide-react';

export interface CatalogSearchProps {
  value: string;
  onSearch: (value: string) => void;
  placeholder?: string;
  className?: string;
}

export const CatalogSearch: FC<CatalogSearchProps> = ({
  value,
  onSearch,
  placeholder = 'Search spawn, cultures, substrate...',
  className = '',
}) => {
  const [term, setTerm] = useState(value);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSearch(term);
  };

  const handleClear = () => {
    setTerm('');
    onSearch('');
  };

  return (
    <form onSubmit={handleSubmit} className={`catalog-search-form ${className}`} style={{ display: 'flex', gap: '0.5rem', width: '100%' }}>
      <div style={{ position: 'relative', flex: 1 }}>
        <Input
          value={term}
          onChange={(e) => setTerm(e.target.value)}
          placeholder={placeholder}
          aria-label="Search products"
          style={{ paddingRight: term ? '2.5rem' : '1rem' }}
        />
        {term && (
          <button
            type="button"
            aria-label="Clear search"
            onClick={handleClear}
            style={{
              position: 'absolute',
              right: '0.75rem',
              top: '50%',
              transform: 'translateY(-50%)',
              background: 'none',
              border: 'none',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
            }}
          >
            <X size={16} />
          </button>
        )}
      </div>
      <button type="submit" className="btn btn-primary" aria-label="Submit search">
        <Search size={18} />
      </button>
    </form>
  );
};
