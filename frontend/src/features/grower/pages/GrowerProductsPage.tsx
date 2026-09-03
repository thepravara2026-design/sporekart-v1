import { FC, useState } from 'react';
import { useGrowerProducts } from '../hooks/useGrowerProducts';
import { GrowerProductTable } from '../components/GrowerProductTable';
import { GrowerProductCard } from '../components/GrowerProductCard';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { GrowerEmptyState } from '../components/GrowerEmptyState';
import { Input } from '../../../components/ui/Input';
import { Button } from '../../../components/ui/Button';

export const GrowerProductsPage: FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState<'table' | 'grid'>('table');
  const { products, isLoading, isError, refetch } = useGrowerProducts({ search: searchTerm });

  if (isLoading) {
    return <GrowerSkeleton type="table" count={5} />;
  }

  if (isError) {
    return <GrowerErrorState onRetry={refetch} />;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
            Grower Catalog Products
          </h1>
          <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
            Manage active spawn items, liquid cultures, and substrate products.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <Button
            size="sm"
            variant={viewMode === 'table' ? 'primary' : 'secondary'}
            onClick={() => setViewMode('table')}
          >
            Table View
          </Button>
          <Button
            size="sm"
            variant={viewMode === 'grid' ? 'primary' : 'secondary'}
            onClick={() => setViewMode('grid')}
          >
            Grid View
          </Button>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <Input
          type="search"
          placeholder="Search by SKU, product name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ maxWidth: '360px' }}
        />
      </div>

      {products.length === 0 ? (
        <GrowerEmptyState
          title="No Products Found"
          description="No catalog products match your search query."
          actionLabel="Clear Search"
          onAction={() => setSearchTerm('')}
        />
      ) : viewMode === 'table' ? (
        <GrowerProductTable products={products} />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.25rem' }}>
          {products.map((product) => (
            <GrowerProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
};
