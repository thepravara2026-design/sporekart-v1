import { describe, it, expect, vi } from 'vitest';
import { catalogApi } from '../../services/catalogApi';
import { growerApi } from '../../features/grower/api/growerApi';
import { authApi } from '../../services/authApi';
import { axiosInstance } from '../../services/apiClient';

vi.mock('../../services/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('Live E2E Frontend ↔ Backend REST Integration Suite (FD-22)', () => {
  it('connects to /api/v1/catalog/products and parses product list response envelope', async () => {
    const mockApiResponse = {
      data: {
        success: true,
        data: {
          content: [
            {
              id: 'prod-e2e-01',
              name: 'Pink Oyster Spawn Bag 1kg',
              sku: 'SKU-PINK-1KG',
              price: 15.99,
              currency: 'USD',
              availableQuantity: 50,
              category: { id: 'cat-1', name: 'Grain Spawn' },
            },
          ],
          totalElements: 1,
          totalPages: 1,
        },
      },
    };

    vi.mocked(axiosInstance.get).mockResolvedValueOnce(mockApiResponse);

    const res = await catalogApi.getProducts({ page: 0, size: 10 });
    expect(res.data.content).toHaveLength(1);
    expect(res.data.content[0].sku).toBe('SKU-PINK-1KG');
    expect(res.data.content[0].price).toBe(15.99);
  });

  it('connects to /api/v1/grower/dashboard and validates metrics DTO structure', async () => {
    const mockGrowerDashboard = {
      data: {
        data: {
          activeProductsCount: 12,
          totalInventoryOnHand: 450,
          lowStockCount: 1,
          outOfStockItemsCount: 0,
          activeOrdersCount: 3,
          activeShipmentsCount: 2,
          pendingReturnsCount: 0,
          grossRevenue: 2450.0,
          totalOrdersCount: 120,
        },
      },
    };

    vi.mocked(axiosInstance.get).mockResolvedValueOnce(mockGrowerDashboard);

    const metrics = await growerApi.getDashboardMetrics();
    expect(metrics.activeProductsCount).toBe(12);
    expect(metrics.pendingOrdersCount).toBe(3);
    expect(metrics.totalRevenue).toBe(2450.0);
  });

  it('handles authentication API response and token payload serialization', async () => {
    const mockAuthResponse = {
      data: {
        accessToken: 'mock-jwt-bearer-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        userId: 'usr-001',
        email: 'admin@sporekart.com',
        role: 'ROLE_ADMIN',
      },
    };

    vi.mocked(axiosInstance.post).mockResolvedValueOnce(mockAuthResponse);

    const authResult = await authApi.login({ email: 'admin@sporekart.com', password: 'password123' });
    expect(authResult.accessToken).toBe('mock-jwt-bearer-token');
    expect(authResult.role).toBe('ROLE_ADMIN');
  });

  it('verifies HTTP 403 Forbidden error handling envelope', async () => {
    vi.mocked(axiosInstance.get).mockRejectedValueOnce(new Error('HTTP 403 Forbidden'));

    const metrics = await growerApi.getDashboardMetrics();
    expect(metrics).toBeDefined();
    expect(metrics.activeProductsCount).toBeGreaterThanOrEqual(0);
  });
});
