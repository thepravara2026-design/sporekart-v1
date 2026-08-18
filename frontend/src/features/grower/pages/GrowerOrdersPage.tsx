import { FC, useState } from 'react';
import { useGrowerOrders } from '../hooks/useGrowerOrders';
import { GrowerOrderTable } from '../components/GrowerOrderTable';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { GrowerEmptyState } from '../components/GrowerEmptyState';

export const GrowerOrdersPage: FC = () => {
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const { orders, isLoading, isError, refetch, transitionOrder, isTransitioning } = useGrowerOrders();

  if (isLoading) {
    return <GrowerSkeleton type="table" count={5} />;
  }

  if (isError) {
    return <GrowerErrorState onRetry={refetch} />;
  }

  const filteredOrders = orders.filter((o) => (filterStatus === 'ALL' ? true : o.status === filterStatus));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Order Visibility & Fulfillment
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Inspect incoming customer orders, manage processing pipeline, and execute fulfillment actions.
        </p>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        {['ALL', 'PROCESSING', 'READY_FOR_FULFILMENT', 'SHIPPED', 'COMPLETED', 'CANCELLED'].map((status) => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            style={{
              padding: '0.375rem 0.75rem',
              borderRadius: '0.375rem',
              fontSize: '0.875rem',
              fontWeight: 500,
              backgroundColor: filterStatus === status ? '#10b981' : '#0d231a',
              color: filterStatus === status ? '#ffffff' : '#9ca3af',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              cursor: 'pointer',
            }}
          >
            {status.replace(/_/g, ' ')}
          </button>
        ))}
      </div>

      {filteredOrders.length === 0 ? (
        <GrowerEmptyState
          title="No Orders Found"
          description="There are currently no orders under the selected status filter."
          actionLabel="Show All Orders"
          onAction={() => setFilterStatus('ALL')}
        />
      ) : (
        <GrowerOrderTable
          orders={filteredOrders}
          onTransitionOrder={async (payload) => {
            await transitionOrder(payload);
          }}
          isTransitioning={isTransitioning}
        />
      )}
    </div>
  );
};
