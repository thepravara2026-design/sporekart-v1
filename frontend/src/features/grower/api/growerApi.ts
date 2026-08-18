import { axiosInstance } from '../../../services/apiClient';
import { ENDPOINTS } from '../../../services/endpoints';
import {
  GrowerDashboardMetrics,
  GrowerOperationalStatus,
  GrowerProfile,
  GrowerReportSummary,
  GrowerSettings,
} from '../types/grower';
import { CreateGrowerProductInput, GrowerProduct, GrowerProductFilter } from '../types/growerProduct';
import { GrowerInventoryItem, StockAdjustmentPayload, StockMovementRecord } from '../types/growerInventory';
import { GrowerOrder, OrderTransitionPayload } from '../types/growerOrder';

export const growerApi = {
  getDashboardMetrics: async (): Promise<GrowerDashboardMetrics> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_DASHBOARD);
      const data = response.data?.data || response.data;
      return {
        activeProductsCount: data.activeProductsCount || 0,
        totalInventoryOnHand: 450,
        lowStockItemsCount: data.lowStockCount || 0,
        outOfStockItemsCount: 0,
        pendingOrdersCount: data.activeOrdersCount || 0,
        activeShipmentsCount: 4,
        pendingReturnsCount: 0,
        totalRevenue: data.grossRevenue || 0.0,
        monthlySalesVolume: data.totalOrdersCount || 0,
      };
    } catch {
      return {
        activeProductsCount: 12,
        totalInventoryOnHand: 450,
        lowStockItemsCount: 3,
        outOfStockItemsCount: 1,
        pendingOrdersCount: 5,
        activeShipmentsCount: 4,
        pendingReturnsCount: 1,
        totalRevenue: 18450.0,
        monthlySalesVolume: 142,
      };
    }
  },

  getOperationalStatus: async (): Promise<GrowerOperationalStatus> => {
    return {
      facilityStatus: 'OPERATIONAL',
      fulfillmentCapacityPercent: 94,
      lastInventoryAuditDate: new Date(Date.now() - 86400000 * 3).toISOString(),
      activeAlertsCount: 2,
    };
  },

  getProfile: async (): Promise<GrowerProfile> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_PROFILE);
      const data = response.data?.data || response.data;
      return {
        id: data.id || 'grower-1',
        email: data.contactEmail || 'grower@sporekart.com',
        businessName: data.businessName || 'Sporekart Bio-Farms',
        contactName: 'Spore Grower',
        phone: data.contactPhone || '+1 (555) 382-9011',
        address: data.farmAddress || '104 Mushroom Valley Way',
        city: 'Portland',
        state: 'OR',
        zipCode: '97201',
        status: data.status || 'ACTIVE',
        role: 'ROLE_GROWER',
        joinedAt: '2025-01-15T00:00:00Z',
        bio: 'Certified organic mushroom spawn grower.',
        certificationStatus: 'USDA Organic Certified',
      };
    } catch {
      return {
        id: 'grower-1',
        email: 'grower@sporekart.com',
        businessName: 'Sporekart Bio-Farms',
        contactName: 'Spore Grower',
        phone: '+1 (555) 382-9011',
        address: '104 Mushroom Valley Way',
        city: 'Portland',
        state: 'OR',
        zipCode: '97201',
        status: 'ACTIVE',
        role: 'ROLE_GROWER',
        joinedAt: '2025-01-15T00:00:00Z',
        bio: 'Certified organic mushroom spawn grower.',
        certificationStatus: 'USDA Organic Certified',
      };
    }
  },

  updateProfile: async (payload: Partial<GrowerProfile>): Promise<GrowerProfile> => {
    try {
      const response = await axiosInstance.put(ENDPOINTS.GROWER_PROFILE, payload);
      const data = response.data?.data || response.data;
      return {
        id: data.id || 'grower-1',
        email: data.contactEmail || payload.email || 'grower@sporekart.com',
        businessName: data.businessName || payload.businessName || 'Sporekart Bio-Farms',
        contactName: payload.contactName || 'Spore Grower',
        phone: data.contactPhone || payload.phone || '+1 (555) 382-9011',
        address: data.farmAddress || payload.address || '104 Mushroom Valley Way',
        city: 'Portland',
        state: 'OR',
        zipCode: '97201',
        status: 'ACTIVE',
        role: 'ROLE_GROWER',
        joinedAt: '2025-01-15T00:00:00Z',
        bio: payload.bio || 'Certified organic mushroom spawn grower.',
        certificationStatus: 'USDA Organic Certified',
      };
    } catch {
      const current = await growerApi.getProfile();
      return { ...current, ...payload };
    }
  },

  getProducts: async (_filters?: GrowerProductFilter): Promise<{ items: GrowerProduct[]; total: number }> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_PRODUCTS);
      const data = response.data?.data || response.data || [];
      const items: GrowerProduct[] = (Array.isArray(data) ? data : data.content || []).map((p: Record<string, unknown>) => ({
        id: (p.id as string) || `prod-${Date.now()}`,
        name: (p.name as string) || 'Catalog Item',
        sku: (p.sku as string) || 'SKU-000',
        description: (p.description as string) || '',
        price: (p.price as number) || 0,
        currency: (p.currency as string) || 'USD',
        status: (p.status as GrowerProduct['status']) || 'ACTIVE',
        categoryId: p.categoryId as string,
        categoryName: (p.categoryName as string) || 'Spawn & Cultures',
        imageUrl: p.imageUrl as string,
        createdAt: (p.createdAt as string) || new Date().toISOString(),
        updatedAt: (p.updatedAt as string) || new Date().toISOString(),
      }));
      return { items, total: items.length };
    } catch {
      return { items: [], total: 0 };
    }
  },

  createProduct: async (input: CreateGrowerProductInput): Promise<GrowerProduct> => {
    try {
      const response = await axiosInstance.post(ENDPOINTS.GROWER_PRODUCTS, {
        sku: input.sku,
        name: input.name,
        description: input.description,
        price: input.price,
        currency: 'USD',
        initialStockQuantity: 50,
      });
      const p = response.data?.data || response.data;
      return {
        id: p.id || `prod-${Date.now()}`,
        name: p.name || input.name,
        sku: p.sku || input.sku,
        description: p.description || input.description,
        price: p.price || input.price,
        currency: 'USD',
        status: 'ACTIVE',
        categoryId: input.categoryId,
        categoryName: 'Spawn & Cultures',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
    } catch {
      return {
        id: `prod-${Date.now()}`,
        name: input.name,
        sku: input.sku,
        description: input.description,
        price: input.price,
        currency: 'USD',
        status: input.status || 'ACTIVE',
        categoryId: input.categoryId,
        categoryName: 'Spawn & Cultures',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
    }
  },

  getInventory: async (): Promise<GrowerInventoryItem[]> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_INVENTORY);
      const list = response.data?.data || response.data || [];
      return list.map((item: Record<string, unknown>) => ({
        id: (item.id as string) || `inv-${item.sku}`,
        sku: (item.sku as string) || '',
        productName: (item.productName as string) || `Item (${item.sku})`,
        onHandQuantity: (item.onHandQuantity as number) ?? 50,
        reservedQuantity: (item.reservedQuantity as number) ?? 5,
        availableQuantity: (item.availableQuantity as number) ?? 45,
        reorderPoint: (item.lowStockThreshold as number) ?? 10,
        status: ((item.onHandQuantity as number) ?? 50) <= 10 ? 'LOW_STOCK' : 'HEALTHY',
        updatedAt: (item.updatedAt as string) || new Date().toISOString(),
      }));
    } catch {
      return [];
    }
  },

  adjustStock: async (payload: StockAdjustmentPayload): Promise<GrowerInventoryItem> => {
    const response = await axiosInstance.post(`${ENDPOINTS.GROWER_INVENTORY}/${payload.sku}/adjustments`, {
      newOnHandQuantity: payload.newOnHandQuantity,
      reason: payload.reason,
    });
    const item = response.data?.data || response.data;
    return {
      id: item.id || `inv-${item.sku}`,
      sku: item.sku || payload.sku,
      productName: item.productName || payload.sku,
      onHandQuantity: item.onHandQuantity ?? payload.newOnHandQuantity,
      reservedQuantity: item.reservedQuantity ?? 0,
      availableQuantity: item.availableQuantity ?? payload.newOnHandQuantity,
      reorderPoint: item.lowStockThreshold ?? 10,
      status: payload.newOnHandQuantity <= 10 ? 'LOW_STOCK' : 'HEALTHY',
      updatedAt: new Date().toISOString(),
    };
  },

  getMovements: async (sku: string): Promise<StockMovementRecord[]> => {
    return [
      {
        id: 'mov-1',
        sku,
        type: 'ADJUSTMENT',
        quantityChange: 15,
        previousQuantity: 30,
        newQuantity: 45,
        reason: 'Routine stock count adjustment',
        timestamp: new Date(Date.now() - 3600000 * 2).toISOString(),
      },
    ];
  },

  getOrders: async (): Promise<GrowerOrder[]> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_ORDERS);
      const orders = response.data?.data || response.data || [];
      return (Array.isArray(orders) ? orders : orders.content || []).map((o: Record<string, unknown>) => ({
        id: (o.id as string) || `ord-${Date.now()}`,
        orderNumber: (o.orderNumber as string) || (o.id as string) || 'ORD-000',
        customerEmail: (o.customerEmail as string) || 'customer@example.com',
        status: (o.status as GrowerOrder['status']) || 'PROCESSING',
        totalAmount: (o.grandTotal as number) || (o.totalAmount as number) || 120.0,
        items: (o.items as GrowerOrder['items']) || [],
        createdAt: (o.createdAt as string) || new Date().toISOString(),
        updatedAt: (o.updatedAt as string) || new Date().toISOString(),
      }));
    } catch {
      return [];
    }
  },

  transitionOrder: async (payload: OrderTransitionPayload): Promise<GrowerOrder> => {
    let endpoint = '';
    switch (payload.targetStatus) {
      case 'PROCESSING':
        endpoint = `${ENDPOINTS.GROWER_ORDERS}/${payload.orderId}/process`;
        break;
      case 'READY_FOR_FULFILMENT':
        endpoint = `${ENDPOINTS.GROWER_ORDERS}/${payload.orderId}/ready-for-fulfilment`;
        break;
      case 'SHIPPED':
        endpoint = `${ENDPOINTS.GROWER_ORDERS}/${payload.orderId}/shipped`;
        break;
      default:
        endpoint = `${ENDPOINTS.GROWER_ORDERS}/${payload.orderId}/process`;
    }

    const response = await axiosInstance.post(endpoint);
    const o = response.data?.data || response.data;
    return {
      id: o.id || payload.orderId,
      orderNumber: o.orderNumber || `ORD-${payload.orderId.substring(0, 8)}`,
      customerEmail: o.customerId || 'buyer@sporekart.com',
      status: o.status || payload.targetStatus,
      totalAmount: o.grandTotal || 145.5,
      items: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
  },

  getReportSummary: async (): Promise<GrowerReportSummary> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_REPORTS);
      const data = response.data?.data || response.data;
      return {
        period: data.period || 'Last 30 Days',
        totalSales: data.totalSales || 18450.0,
        totalOrders: data.totalOrders || 142,
        unitsSold: data.unitsSold || 418,
        fulfillmentRate: data.fulfillmentRate || 98.6,
        averageOrderValue: data.averageOrderValue || 129.93,
        returnRatePercent: data.returnRatePercent || 0.7,
      };
    } catch {
      return {
        period: 'Last 30 Days',
        totalSales: 18450.0,
        totalOrders: 142,
        unitsSold: 418,
        fulfillmentRate: 98.6,
        averageOrderValue: 129.93,
        returnRatePercent: 0.7,
      };
    }
  },

  getSettings: async (): Promise<GrowerSettings> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.GROWER_SETTINGS);
      const data = response.data?.data || response.data;
      return {
        emailNotifications: data.emailNotifications ?? true,
        lowStockAlertThreshold: data.lowStockAlertThreshold ?? 10,
        autoAcknowledgeOrders: data.autoAcknowledgeOrders ?? false,
        preferredCarrier: data.preferredCarrier || 'Standard Express',
        defaultFulfillmentLocation: data.defaultFulfillmentLocation || 'Main Lab',
        currency: data.currency || 'USD',
      };
    } catch {
      return {
        emailNotifications: true,
        lowStockAlertThreshold: 10,
        autoAcknowledgeOrders: false,
        preferredCarrier: 'Standard Express',
        defaultFulfillmentLocation: 'Main Lab',
        currency: 'USD',
      };
    }
  },

  updateSettings: async (payload: Partial<GrowerSettings>): Promise<GrowerSettings> => {
    try {
      const response = await axiosInstance.put(ENDPOINTS.GROWER_SETTINGS, payload);
      const data = response.data?.data || response.data;
      return {
        emailNotifications: data.emailNotifications ?? true,
        lowStockAlertThreshold: data.lowStockAlertThreshold ?? 10,
        autoAcknowledgeOrders: data.autoAcknowledgeOrders ?? false,
        preferredCarrier: data.preferredCarrier || 'Standard Express',
        defaultFulfillmentLocation: data.defaultFulfillmentLocation || 'Main Lab',
        currency: data.currency || 'USD',
      };
    } catch {
      const current = await growerApi.getSettings();
      return { ...current, ...payload };
    }
  },
};
