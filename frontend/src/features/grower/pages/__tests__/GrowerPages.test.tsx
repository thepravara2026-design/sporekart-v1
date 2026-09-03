import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { GrowerDashboardPage } from '../GrowerDashboardPage';
import { GrowerProfilePage } from '../GrowerProfilePage';
import { GrowerProductsPage } from '../GrowerProductsPage';
import { GrowerInventoryPage } from '../GrowerInventoryPage';
import { GrowerOrdersPage } from '../GrowerOrdersPage';
import { GrowerShipmentsPage } from '../GrowerShipmentsPage';
import { GrowerReportsPage } from '../GrowerReportsPage';
import { GrowerSettingsPage } from '../GrowerSettingsPage';

// Mock hook responses
vi.mock('../../hooks/useGrowerDashboard', () => ({
  useGrowerDashboard: () => ({
    metrics: {
      activeProductsCount: 12,
      totalInventoryOnHand: 450,
      lowStockItemsCount: 2,
      outOfStockItemsCount: 0,
      pendingOrdersCount: 4,
      activeShipmentsCount: 3,
      pendingReturnsCount: 0,
      totalRevenue: 18450.0,
      monthlySalesVolume: 142,
    },
    operationalStatus: {
      facilityStatus: 'OPERATIONAL',
      fulfillmentCapacityPercent: 95,
      lastInventoryAuditDate: '2026-08-15T00:00:00Z',
      activeAlertsCount: 1,
    },
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));

vi.mock('../../hooks/useGrowerProfile', () => ({
  useGrowerProfile: () => ({
    profile: {
      id: 'g1',
      email: 'grower@sporekart.com',
      businessName: 'Sporekart Bio-Farms',
      contactName: 'Master Grower',
      phone: '+1 555-0192',
      status: 'ACTIVE',
      role: 'ROLE_ADMIN',
      joinedAt: '2025-01-01',
    },
    isLoading: false,
    isError: false,
    updateProfile: vi.fn(),
    isUpdating: false,
  }),
}));

vi.mock('../../hooks/useGrowerProducts', () => ({
  useGrowerProducts: () => ({
    products: [
      {
        id: 'p1',
        name: "Lion's Mane Spawn",
        sku: 'SKU-LION-001',
        price: 35.0,
        currency: 'INR',
        status: 'ACTIVE',
        categoryName: 'Spawn',
      },
    ],
    totalCount: 1,
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));

vi.mock('../../hooks/useGrowerInventory', () => ({
  useGrowerInventory: () => ({
    inventory: [
      {
        id: 'inv-1',
        sku: 'SKU-LION-001',
        productName: "Lion's Mane Spawn",
        onHandQuantity: 50,
        reservedQuantity: 5,
        availableQuantity: 45,
        reorderPoint: 10,
        status: 'HEALTHY',
        updatedAt: '2026-08-15',
      },
    ],
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
    adjustStock: vi.fn(),
    isAdjusting: false,
  }),
}));

vi.mock('../../hooks/useGrowerOrders', () => ({
  useGrowerOrders: () => ({
    orders: [
      {
        id: 'ord-1',
        orderNumber: 'ORD-2026-001',
        customerEmail: 'buyer@example.com',
        status: 'PROCESSING',
        totalAmount: 120.0,
        items: [],
        createdAt: '2026-08-15',
        updatedAt: '2026-08-15',
      },
    ],
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
    transitionOrder: vi.fn(),
    isTransitioning: false,
  }),
}));

vi.mock('../../hooks/useGrowerShipments', () => ({
  useGrowerShipments: () => ({
    shipments: [
      {
        id: 'shp-1',
        shipmentReference: 'SHP-9901',
        orderReference: 'ORD-2026-001',
        carrier: 'FedEx Express',
        trackingNumber: 'TRK-10293',
        status: 'IN_TRANSIT',
        createdAt: '2026-08-15',
      },
    ],
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));

vi.mock('../../hooks/useGrowerReports', () => ({
  useGrowerReports: () => ({
    reportSummary: {
      period: 'Last 30 Days',
      totalSales: 18450.0,
      totalOrders: 142,
      unitsSold: 418,
      fulfillmentRate: 98.6,
      averageOrderValue: 129.93,
      returnRatePercent: 0.7,
    },
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));

vi.mock('../../hooks/useGrowerSettings', () => ({
  useGrowerSettings: () => ({
    settings: {
      emailNotifications: true,
      lowStockAlertThreshold: 10,
      preferredCarrier: 'FedEx Express',
      defaultFulfillmentLocation: 'Portland Main Lab',
      currency: 'INR',
    },
    isLoading: false,
    isError: false,
    updateSettings: vi.fn(),
    isUpdating: false,
  }),
}));

const renderWithProviders = (component: React.ReactNode) => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{component}</BrowserRouter>
    </QueryClientProvider>
  );
};

describe('Grower Portal Pages', () => {
  it('renders GrowerDashboardPage cleanly', () => {
    renderWithProviders(<GrowerDashboardPage />);
    expect(screen.getByText('Grower Operations Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Active Products')).toBeInTheDocument();
    expect(screen.getByText('12')).toBeInTheDocument();
  });

  it('renders GrowerProfilePage cleanly', () => {
    renderWithProviders(<GrowerProfilePage />);
    expect(screen.getByText('Grower Business Profile')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Sporekart Bio-Farms')).toBeInTheDocument();
  });

  it('renders GrowerProductsPage cleanly', () => {
    renderWithProviders(<GrowerProductsPage />);
    expect(screen.getByText('Grower Catalog Products')).toBeInTheDocument();
    expect(screen.getByText("Lion's Mane Spawn")).toBeInTheDocument();
  });

  it('renders GrowerInventoryPage cleanly', () => {
    renderWithProviders(<GrowerInventoryPage />);
    expect(screen.getByText('Grower Inventory Management')).toBeInTheDocument();
    expect(screen.getByText('SKU-LION-001')).toBeInTheDocument();
  });

  it('renders GrowerOrdersPage cleanly', () => {
    renderWithProviders(<GrowerOrdersPage />);
    expect(screen.getByText('Grower Order Visibility & Fulfillment')).toBeInTheDocument();
    expect(screen.getByText('ORD-2026-001')).toBeInTheDocument();
  });

  it('renders GrowerShipmentsPage cleanly', () => {
    renderWithProviders(<GrowerShipmentsPage />);
    expect(screen.getByText('Grower Shipment & Fulfillment Tracking')).toBeInTheDocument();
    expect(screen.getByText('SHP-9901')).toBeInTheDocument();
  });

  it('renders GrowerReportsPage cleanly', () => {
    renderWithProviders(<GrowerReportsPage />);
    expect(screen.getByText('Grower Operational Analytics & Reporting')).toBeInTheDocument();
    expect(screen.getByText(/₹\s*18,450\.00/)).toBeInTheDocument();
  });

  it('renders GrowerSettingsPage cleanly', () => {
    renderWithProviders(<GrowerSettingsPage />);
    expect(screen.getByText('Grower Operational Settings')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Portland Main Lab')).toBeInTheDocument();
  });
});
