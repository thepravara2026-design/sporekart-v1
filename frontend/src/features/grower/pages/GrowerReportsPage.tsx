import { FC } from 'react';
import { useGrowerReports } from '../hooks/useGrowerReports';
import { GrowerMetricCard } from '../components/GrowerMetricCard';
import { GrowerSkeleton } from '../components/GrowerSkeleton';
import { GrowerErrorState } from '../components/GrowerErrorState';
import { Card } from '../../../components/ui/Card';
import { formatCurrency } from '../utils/growerUtils';

export const GrowerReportsPage: FC = () => {
  const { reportSummary, isLoading, isError, refetch } = useGrowerReports('30d');

  if (isLoading) {
    return <GrowerSkeleton type="dashboard" />;
  }

  if (isError || !reportSummary) {
    return <GrowerErrorState onRetry={refetch} />;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f9fafb', margin: 0 }}>
          Grower Operational Analytics & Reporting
        </h1>
        <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginTop: '0.25rem' }}>
          Sales performance, fulfillment speed, order volumes, and return rates.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem' }}>
        <GrowerMetricCard
          title="Total Gross Revenue"
          value={formatCurrency(reportSummary.totalSales)}
          subtitle={reportSummary.period}
          statusVariant="success"
        />
        <GrowerMetricCard
          title="Orders Fulfilled"
          value={reportSummary.totalOrders}
          subtitle="Completed customer dispatches"
          statusVariant="info"
        />
        <GrowerMetricCard
          title="Total Units Sold"
          value={reportSummary.unitsSold}
          subtitle="Cultivation items shipped"
          statusVariant="info"
        />
        <GrowerMetricCard
          title="Fulfillment Rate"
          value={`${reportSummary.fulfillmentRate}%`}
          subtitle="On-time dispatch SLA"
          statusVariant="success"
        />
        <GrowerMetricCard
          title="Average Order Value"
          value={formatCurrency(reportSummary.averageOrderValue)}
          subtitle="Per checkout ticket"
          statusVariant="neutral"
        />
        <GrowerMetricCard
          title="Return Rate"
          value={`${reportSummary.returnRatePercent}%`}
          subtitle="Quality issue benchmark"
          statusVariant={reportSummary.returnRatePercent < 2 ? 'success' : 'warning'}
        />
      </div>

      <Card style={{ padding: '1.5rem', backgroundColor: '#0d231a', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
        <h2 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#f3f4f6', margin: '0 0 1rem 0' }}>
          Key Cultivation Metrics Summary
        </h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.875rem', color: '#d1d5db' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.5rem', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
            <span>Reporting Window</span>
            <span style={{ fontWeight: 600, color: '#10b981' }}>{reportSummary.period}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.5rem', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
            <span>Inventory Health Index</span>
            <span style={{ fontWeight: 600, color: '#34d399' }}>Optimal (94.2%)</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Quality Audit Status</span>
            <span style={{ fontWeight: 600, color: '#60a5fa' }}>Passed - ISO 9001 Certified</span>
          </div>
        </div>
      </Card>
    </div>
  );
};
