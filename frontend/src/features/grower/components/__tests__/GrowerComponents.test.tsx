import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { GrowerMetricCard } from '../GrowerMetricCard';
import { GrowerStatusBadge } from '../GrowerStatusBadge';
import { GrowerProductCard } from '../GrowerProductCard';
import { GrowerStockStatus } from '../GrowerStockStatus';
import { GrowerOrderStatus } from '../GrowerOrderStatus';
import { GrowerShipmentStatus } from '../GrowerShipmentStatus';
import { GrowerEmptyState } from '../GrowerEmptyState';
import { GrowerErrorState } from '../GrowerErrorState';
import { GrowerProduct } from '../../types/growerProduct';

describe('Grower UI Components', () => {
  it('renders GrowerMetricCard title, value, and subtitle', () => {
    render(<GrowerMetricCard title="Active Products" value={14} subtitle="In catalog" statusVariant="success" />);
    expect(screen.getByText('Active Products')).toBeInTheDocument();
    expect(screen.getByText('14')).toBeInTheDocument();
    expect(screen.getByText('In catalog')).toBeInTheDocument();
  });

  it('renders GrowerStatusBadge with label', () => {
    render(<GrowerStatusBadge label="ACTIVE" variant="success" />);
    expect(screen.getByText('ACTIVE')).toBeInTheDocument();
  });

  it('renders GrowerProductCard details', () => {
    const mockProduct: GrowerProduct = {
      id: 'p1',
      name: 'Organic Oyster Spawn',
      sku: 'SKU-OYST-001',
      price: 29.99,
      currency: 'INR',
      status: 'ACTIVE',
      categoryName: 'Spawn',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    };
    render(<GrowerProductCard product={mockProduct} />);
    expect(screen.getByText('Organic Oyster Spawn')).toBeInTheDocument();
    expect(screen.getByText('SKU-OYST-001')).toBeInTheDocument();
    expect(screen.getByText(/₹\s*29\.99/)).toBeInTheDocument();
  });

  it('renders GrowerStockStatus indicator', () => {
    render(<GrowerStockStatus onHandQuantity={50} reservedQuantity={5} reorderPoint={10} />);
    expect(screen.getByText('Healthy')).toBeInTheDocument();
    expect(screen.getByText('(45 avail)')).toBeInTheDocument();
  });

  it('renders GrowerOrderStatus badge', () => {
    render(<GrowerOrderStatus status="PROCESSING" />);
    expect(screen.getByText('Processing')).toBeInTheDocument();
  });

  it('renders GrowerShipmentStatus badge', () => {
    render(<GrowerShipmentStatus status="IN_TRANSIT" />);
    expect(screen.getByText('IN TRANSIT')).toBeInTheDocument();
  });

  it('renders GrowerEmptyState correctly', () => {
    render(<GrowerEmptyState title="No Items" description="Nothing found here." />);
    expect(screen.getByText('No Items')).toBeInTheDocument();
    expect(screen.getByText('Nothing found here.')).toBeInTheDocument();
  });

  it('renders GrowerErrorState message', () => {
    render(<GrowerErrorState message="Custom operational error" />);
    expect(screen.getByText('Custom operational error')).toBeInTheDocument();
  });
});
