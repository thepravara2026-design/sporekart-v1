import { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, Boxes, AlertTriangle, ClipboardList, Truck, IndianRupee } from 'lucide-react';
import { useGrowerDashboard } from '../hooks/useGrowerDashboard';
import { GrowerMetricCard } from '../components/GrowerMetricCard';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { Alert } from '../../../components/ui/Alert';
import { formatCurrency } from '../utils/growerUtils';

export const GrowerDashboardPage: FC = () => {
  const navigate = useNavigate();
  const { metrics, operationalStatus, isLoading, isError, refetch } = useGrowerDashboard();

  if (isLoading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#f9fafb' }}>Grower Operations Dashboard</h1>
        <GrowerSkeleton type="dashboard" />
      </div>
    );
  }

  if (isError || !metrics) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#f9fafb' }}>Grower Operations Dashboard</h1>
        <GrowerErrorState onRetry={refetch} />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Operations Dashboard
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Real-time catalog, stock health, fulfillment, and revenue metrics.
        </p>
      </div>

      {operationalStatus && operationalStatus.activeAlertsCount > 0 && (
        <Alert variant="warning">
          Facility Status: {operationalStatus.facilityStatus}. Capacity at {operationalStatus.fulfillmentCapacityPercent}%. {operationalStatus.activeAlertsCount} active inventory alert(s) require attention.
        </Alert>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem' }}>
        <GrowerMetricCard
          title="Active Products"
          value={metrics.activeProductsCount}
          subtitle="Listed in Sporekart catalog"
          statusVariant="success"
          icon={<Package size={20} />}
          onClick={() => navigate('/grower/products')}
        />
        <GrowerMetricCard
          title="Inventory On Hand"
          value={metrics.totalInventoryOnHand}
          subtitle="Total units across batches"
          statusVariant="info"
          icon={<Boxes size={20} />}
          onClick={() => navigate('/grower/inventory')}
        />
        <GrowerMetricCard
          title="Low Stock Alert"
          value={metrics.lowStockItemsCount}
          subtitle="Items near reorder point"
          statusVariant={metrics.lowStockItemsCount > 0 ? 'warning' : 'success'}
          icon={<AlertTriangle size={20} />}
          onClick={() => navigate('/grower/inventory')}
        />
        <GrowerMetricCard
          title="Pending Orders"
          value={metrics.pendingOrdersCount}
          subtitle="Orders requiring action"
          statusVariant={metrics.pendingOrdersCount > 0 ? 'warning' : 'neutral'}
          icon={<ClipboardList size={20} />}
          onClick={() => navigate('/grower/orders')}
        />
        <GrowerMetricCard
          title="Active Shipments"
          value={metrics.activeShipmentsCount}
          subtitle="In transit to customers"
          statusVariant="info"
          icon={<Truck size={20} />}
          onClick={() => navigate('/grower/shipments')}
        />
        <GrowerMetricCard
          title="Total Revenue"
          value={formatCurrency(metrics.totalRevenue)}
          subtitle="30-day gross revenue"
          statusVariant="success"
          icon={<IndianRupee size={20} />}
          onClick={() => navigate('/grower/reports')}
        />
      </div>
    </div>
  );
};
