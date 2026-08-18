import { FC } from 'react';
import { useGrowerShipments } from '../hooks/useGrowerShipments';
import { GrowerShipmentStatus } from '../components/GrowerShipmentStatus';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { GrowerEmptyState } from '../components/GrowerEmptyState';

export const GrowerShipmentsPage: FC = () => {
  const { shipments, isLoading, isError, refetch } = useGrowerShipments();

  if (isLoading) {
    return <GrowerSkeleton type="table" count={4} />;
  }

  if (isError) {
    return <GrowerErrorState onRetry={refetch} />;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Shipment & Fulfillment Tracking
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Carrier dispatches, tracking references, and delivery milestones.
        </p>
      </div>

      {shipments.length === 0 ? (
        <GrowerEmptyState
          title="No Active Shipments"
          description="There are currently no active shipments in transit."
        />
      ) : (
        <div style={{ overflowX: 'auto', borderRadius: '0.5rem', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#0d231a', fontSize: '0.875rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.1)', color: '#9ca3af', backgroundColor: 'rgba(0, 0, 0, 0.2)' }}>
                <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Shipment Ref</th>
                <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Order Ref</th>
                <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Carrier</th>
                <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Tracking Number</th>
                <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Status</th>
                <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Est. Delivery</th>
              </tr>
            </thead>
            <tbody>
              {shipments.map((s) => (
                <tr key={s.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)', color: '#e5e7eb' }}>
                  <td style={{ padding: '0.75rem 1rem', fontWeight: 600, color: '#10b981' }}>{s.shipmentReference}</td>
                  <td style={{ padding: '0.75rem 1rem', color: '#d1d5db' }}>{s.orderReference}</td>
                  <td style={{ padding: '0.75rem 1rem', color: '#9ca3af' }}>{s.carrier}</td>
                  <td style={{ padding: '0.75rem 1rem', fontFamily: 'monospace', color: '#34d399' }}>{s.trackingNumber}</td>
                  <td style={{ padding: '0.75rem 1rem' }}>
                    <GrowerShipmentStatus status={s.status} />
                  </td>
                  <td style={{ padding: '0.75rem 1rem', color: '#9ca3af' }}>
                    {s.estimatedDeliveryDate ? new Date(s.estimatedDeliveryDate).toLocaleDateString() : 'N/A'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
