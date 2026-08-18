import React from 'react';
import { SellerLayout } from '../components/SellerLayout';
import { SellerMetricsCard } from '../components/SellerMetricsCard';
import { PayoutSummaryTable } from '../components/PayoutSummaryTable';
import { useSellerMetrics, useSellerPayouts } from '../hooks/useSeller';
import { Button } from '../../../components/ui/Button';

export const SellerDashboardPage: React.FC = () => {
  const { data: metrics, isLoading: isMetricsLoading } = useSellerMetrics();
  const { data: payouts, isLoading: isPayoutsLoading } = useSellerPayouts();

  return (
    <SellerLayout>
      <div data-testid="seller-dashboard-page" className="space-y-8">
        {/* Section Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-100 tracking-tight">Marketplace Overview</h2>
            <p className="text-sm text-slate-400">
              Live performance metrics, inventory sync state, and settlement payouts.
            </p>
          </div>
          <Button variant="primary" size="sm" onClick={() => window.location.assign('/seller/products')}>
            + Manage Product Listings
          </Button>
        </div>

        {/* Sales & Operational Metrics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <SellerMetricsCard
            title="Total Marketplace Revenue"
            value={isMetricsLoading ? '...' : `$${(metrics?.totalSales || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
            subtitle="Lifetime settled revenue"
            trend={{ value: '14.2%', isPositive: true }}
            variant="highlight"
            testId="metric-total-sales"
          />
          <SellerMetricsCard
            title="Monthly Sales Revenue"
            value={isMetricsLoading ? '...' : `$${(metrics?.monthlyRevenue || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
            subtitle="Current calendar month"
            trend={{ value: '8.1%', isPositive: true }}
            testId="metric-monthly-revenue"
          />
          <SellerMetricsCard
            title="Active Product Listings"
            value={isMetricsLoading ? '...' : metrics?.activeListingsCount || 0}
            subtitle="Catalog listings online"
            testId="metric-active-listings"
          />
          <SellerMetricsCard
            title="Inventory On-Hand"
            value={isMetricsLoading ? '...' : `${metrics?.totalInventoryOnHand || 0} units`}
            subtitle="Synced warehouse stock"
            variant={metrics?.syncErrorCount ? 'warning' : 'default'}
            testId="metric-inventory-onhand"
          />
        </div>

        {/* Payout Summary Section */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-slate-200">Payout & Settlement Summary</h3>
            <span className="text-xs text-slate-400 font-mono">
              Pending Payout: ${metrics?.payoutPendingAmount?.toFixed(2) || '0.00'}
            </span>
          </div>
          <PayoutSummaryTable payouts={payouts || []} isLoading={isPayoutsLoading} />
        </div>
      </div>
    </SellerLayout>
  );
};
