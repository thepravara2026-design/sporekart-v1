import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { describe, expect, it } from 'vitest';
import { AuthProvider } from '../../../context/AuthContext';
import { SellerDashboardPage } from '../pages/SellerDashboardPage';
import { SellerProductManagementPage } from '../pages/SellerProductManagementPage';
import { SellerInventoryPage } from '../pages/SellerInventoryPage';
import { SellerOrderManagementPage } from '../pages/SellerOrderManagementPage';

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

const renderWithProviders = (ui: React.ReactElement) => {
  const testQueryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={testQueryClient}>
      <AuthProvider>
        <BrowserRouter>{ui}</BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
};

describe('Seller Marketplace Pages', () => {
  it('renders SellerDashboardPage without crashing', async () => {
    renderWithProviders(<SellerDashboardPage />);
    expect(await screen.findByTestId('seller-dashboard-page')).toBeInTheDocument();
    expect(screen.getByText('Marketplace Overview')).toBeInTheDocument();
  });

  it('renders SellerProductManagementPage without crashing', async () => {
    renderWithProviders(<SellerProductManagementPage />);
    expect(await screen.findByTestId('seller-product-management-page')).toBeInTheDocument();
    expect(screen.getByText('Product Listings Manager')).toBeInTheDocument();
  });

  it('renders SellerInventoryPage without crashing', async () => {
    renderWithProviders(<SellerInventoryPage />);
    expect(await screen.findByTestId('seller-inventory-page')).toBeInTheDocument();
    expect(screen.getByText('Inventory Sync & Stock Control')).toBeInTheDocument();
  });

  it('renders SellerOrderManagementPage without crashing', async () => {
    renderWithProviders(<SellerOrderManagementPage />);
    expect(await screen.findByTestId('seller-order-management-page')).toBeInTheDocument();
    expect(screen.getByText('Order Fulfillment & Management')).toBeInTheDocument();
  });
});
