import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { SellerMetricsCard } from '../components/SellerMetricsCard';
import { InventoryStatusBadge } from '../components/InventoryStatusBadge';
import { PayoutSummaryTable } from '../components/PayoutSummaryTable';
import { SellerProductTable } from '../components/SellerProductTable';
import { SellerInventoryTable } from '../components/SellerInventoryTable';
import { SellerOrderTable } from '../components/SellerOrderTable';

describe('Seller Marketplace UI Components', () => {
  it('renders SellerMetricsCard with correct title and value', () => {
    render(
      <SellerMetricsCard
        title="Total Revenue"
        value="$34,250.00"
        subtitle="Lifetime sales"
        trend={{ value: '14.2%', isPositive: true }}
      />
    );

    expect(screen.getByText('Total Revenue')).toBeInTheDocument();
    expect(screen.getByText('$34,250.00')).toBeInTheDocument();
    expect(screen.getByText('Lifetime sales')).toBeInTheDocument();
    expect(screen.getByText(/14.2%/)).toBeInTheDocument();
  });

  it('renders InventoryStatusBadge with correct variants', () => {
    const { rerender } = render(<InventoryStatusBadge status="SYNCED" />);
    expect(screen.getByText('Synced')).toBeInTheDocument();

    rerender(<InventoryStatusBadge status="SYNC_ERROR" />);
    expect(screen.getByText('Sync Error')).toBeInTheDocument();
  });

  it('renders PayoutSummaryTable with empty and loaded states', () => {
    const { rerender } = render(<PayoutSummaryTable payouts={[]} />);
    expect(screen.getByTestId('payout-summary-table-empty')).toBeInTheDocument();

    const samplePayouts = [
      {
        id: 'pay-1',
        payoutReference: 'PAY-2026-01',
        period: 'Jan 2026',
        amount: 1500.0,
        currency: 'USD',
        status: 'COMPLETED' as const,
        payoutDate: '2026-01-31',
        bankAccountLast4: '1234',
      },
    ];

    rerender(<PayoutSummaryTable payouts={samplePayouts} />);
    expect(screen.getByText('PAY-2026-01')).toBeInTheDocument();
    expect(screen.getByText('$1,500.00')).toBeInTheDocument();
  });

  it('renders SellerProductTable correctly', () => {
    const products = [
      {
        id: 'sprod-1',
        name: 'Oyster Spawn Bag',
        sku: 'SKU-OYSTER-1',
        category: 'Grain Spawn',
        price: 25.0,
        currency: 'USD',
        onHandQuantity: 50,
        reservedQuantity: 5,
        syncStatus: 'SYNCED' as const,
        status: 'ACTIVE' as const,
        updatedAt: '2026-08-19',
      },
    ];

    render(<SellerProductTable products={products} />);
    expect(screen.getByText('Oyster Spawn Bag')).toBeInTheDocument();
    expect(screen.getByText('SKU-OYSTER-1')).toBeInTheDocument();
    expect(screen.getByText('$25.00')).toBeInTheDocument();
  });

  it('renders SellerInventoryTable with stock values', () => {
    const inventory = [
      {
        id: 'sinv-1',
        sku: 'SKU-OYSTER-1',
        productName: 'Oyster Spawn Bag',
        warehouseLocation: 'Shelf A1',
        onHandQuantity: 50,
        reservedQuantity: 5,
        availableQuantity: 45,
        syncStatus: 'SYNCED' as const,
        lastSyncedAt: '2026-08-19',
      },
    ];

    render(<SellerInventoryTable inventory={inventory} />);
    expect(screen.getByText('SKU-OYSTER-1')).toBeInTheDocument();
    expect(screen.getByText('Shelf A1')).toBeInTheDocument();
    expect(screen.getByText('50')).toBeInTheDocument();
  });

  it('renders SellerOrderTable with status badges', () => {
    const orders = [
      {
        id: 'sord-1',
        orderNumber: 'ORD-SEL-1',
        customerName: 'John Doe',
        customerEmail: 'john@example.com',
        itemsCount: 2,
        totalAmount: 50.0,
        currency: 'USD',
        status: 'PROCESSING' as const,
        orderDate: '2026-08-19',
      },
    ];

    render(<SellerOrderTable orders={orders} />);
    expect(screen.getByText('ORD-SEL-1')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('PROCESSING')).toBeInTheDocument();
  });
});
