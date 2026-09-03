import { FC, useState } from 'react';
import { useGrowerInventory } from '../hooks/useGrowerInventory';
import { GrowerInventoryTable } from '../components/GrowerInventoryTable';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { GrowerEmptyState } from '../components/GrowerEmptyState';
import { Input } from '../../../components/ui/Input';

export const GrowerInventoryPage: FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const { inventory, isLoading, isError, refetch, adjustStock, isAdjusting } = useGrowerInventory();

  if (isLoading) {
    return <GrowerSkeleton type="table" count={5} />;
  }

  if (isError) {
    return <GrowerErrorState onRetry={refetch} />;
  }

  const filtered = inventory.filter(
    (item) =>
      item.sku.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.productName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Inventory Management
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Real-time stock quantities, reservations, available supply, and rapid stock adjustment.
        </p>
      </div>

      <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <Input
          type="search"
          placeholder="Filter inventory by SKU or item name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ maxWidth: '360px' }}
        />
      </div>

      {filtered.length === 0 ? (
        <GrowerEmptyState
          title="No Inventory Items Found"
          description="No inventory records match your criteria."
          actionLabel="Clear Filter"
          onAction={() => setSearchTerm('')}
        />
      ) : (
        <GrowerInventoryTable
          inventory={filtered}
          onAdjustStock={async (payload) => {
            await adjustStock(payload);
          }}
          isAdjusting={isAdjusting}
        />
      )}
    </div>
  );
};
