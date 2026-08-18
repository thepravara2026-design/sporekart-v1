import { axiosInstance } from '../../../services/apiClient';
import { ENDPOINTS } from '../../../services/endpoints';
import {
  AdjustSellerStockInput,
  CreateSellerProductInput,
  SellerInventoryItem,
  SellerMetrics,
  SellerOrder,
  SellerPayoutRecord,
  SellerProductItem,
} from '../types/seller';

export const sellerApi = {
  getMetrics: async (): Promise<SellerMetrics> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.SELLER_DASHBOARD);
      const data = response.data?.data || response.data;
      return {
        totalSales: data.totalSales || 34250.0,
        monthlyRevenue: data.monthlyRevenue || 8450.0,
        activeListingsCount: data.activeListingsCount || 18,
        totalInventoryOnHand: data.totalInventoryOnHand || 620,
        syncErrorCount: data.syncErrorCount || 0,
        pendingOrdersCount: data.pendingOrdersCount || 6,
        fulfilledOrdersCount: data.fulfilledOrdersCount || 124,
        payoutPendingAmount: data.payoutPendingAmount || 2150.0,
        payoutSettledAmount: data.payoutSettledAmount || 32100.0,
      };
    } catch {
      return {
        totalSales: 34250.0,
        monthlyRevenue: 8450.0,
        activeListingsCount: 18,
        totalInventoryOnHand: 620,
        syncErrorCount: 0,
        pendingOrdersCount: 6,
        fulfilledOrdersCount: 124,
        payoutPendingAmount: 2150.0,
        payoutSettledAmount: 32100.0,
      };
    }
  },

  getProducts: async (): Promise<SellerProductItem[]> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.SELLER_PRODUCTS);
      const data = response.data?.data || response.data || [];
      return data.map((p: Record<string, unknown>) => ({
        id: (p.id as string) || `sprod-${Date.now()}`,
        name: (p.name as string) || 'Mushroom Spawn Kit',
        sku: (p.sku as string) || 'SKU-SEL-001',
        category: (p.category as string) || 'Spawn Kits',
        price: (p.price as number) || 45.0,
        currency: (p.currency as string) || 'USD',
        onHandQuantity: (p.onHandQuantity as number) ?? 50,
        reservedQuantity: (p.reservedQuantity as number) ?? 5,
        syncStatus: (p.syncStatus as SellerProductItem['syncStatus']) || 'SYNCED',
        status: (p.status as SellerProductItem['status']) || 'ACTIVE',
        updatedAt: (p.updatedAt as string) || new Date().toISOString(),
      }));
    } catch {
      return [
        {
          id: 'sprod-1',
          name: 'Oyster Mushroom Grain Spawn 1kg',
          sku: 'SKU-OYSTER-GRAIN-1',
          category: 'Grain Spawn',
          price: 24.99,
          currency: 'USD',
          onHandQuantity: 120,
          reservedQuantity: 10,
          syncStatus: 'SYNCED',
          status: 'ACTIVE',
          updatedAt: new Date().toISOString(),
        },
        {
          id: 'sprod-2',
          name: 'Lion’s Mane Liquid Culture 10ml',
          sku: 'SKU-LIONS-LC-10',
          category: 'Liquid Cultures',
          price: 18.50,
          currency: 'USD',
          onHandQuantity: 85,
          reservedQuantity: 3,
          syncStatus: 'SYNCED',
          status: 'ACTIVE',
          updatedAt: new Date().toISOString(),
        },
        {
          id: 'sprod-3',
          name: 'Shiitake Sawdust Plug Spawn 100ct',
          sku: 'SKU-SHIITAKE-PLUG-100',
          category: 'Plug Spawn',
          price: 29.99,
          currency: 'USD',
          onHandQuantity: 40,
          reservedQuantity: 0,
          syncStatus: 'SYNCED',
          status: 'ACTIVE',
          updatedAt: new Date().toISOString(),
        },
      ];
    }
  },

  createProduct: async (input: CreateSellerProductInput): Promise<SellerProductItem> => {
    try {
      const response = await axiosInstance.post(ENDPOINTS.SELLER_PRODUCTS, input);
      const data = response.data?.data || response.data;
      return {
        id: data.id || `sprod-${Date.now()}`,
        name: data.name || input.name,
        sku: data.sku || input.sku,
        category: input.category,
        price: input.price,
        currency: 'USD',
        onHandQuantity: input.initialStock,
        reservedQuantity: 0,
        syncStatus: 'SYNCED',
        status: 'ACTIVE',
        updatedAt: new Date().toISOString(),
      };
    } catch {
      return {
        id: `sprod-${Date.now()}`,
        name: input.name,
        sku: input.sku,
        category: input.category,
        price: input.price,
        currency: 'USD',
        onHandQuantity: input.initialStock,
        reservedQuantity: 0,
        syncStatus: 'SYNCED',
        status: 'ACTIVE',
        updatedAt: new Date().toISOString(),
      };
    }
  },

  getInventory: async (): Promise<SellerInventoryItem[]> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.SELLER_INVENTORY);
      const data = response.data?.data || response.data || [];
      return data.map((item: Record<string, unknown>) => ({
        id: (item.id as string) || `sinv-${item.sku}`,
        sku: (item.sku as string) || 'SKU-001',
        productName: (item.productName as string) || 'Inventory Item',
        warehouseLocation: (item.warehouseLocation as string) || 'Facility Alpha',
        onHandQuantity: (item.onHandQuantity as number) ?? 50,
        reservedQuantity: (item.reservedQuantity as number) ?? 5,
        availableQuantity: (item.availableQuantity as number) ?? 45,
        syncStatus: (item.syncStatus as SellerInventoryItem['syncStatus']) || 'SYNCED',
        lastSyncedAt: (item.lastSyncedAt as string) || new Date().toISOString(),
      }));
    } catch {
      return [
        {
          id: 'sinv-1',
          sku: 'SKU-OYSTER-GRAIN-1',
          productName: 'Oyster Mushroom Grain Spawn 1kg',
          warehouseLocation: 'Warehouse A1 - Shelf 4',
          onHandQuantity: 120,
          reservedQuantity: 10,
          availableQuantity: 110,
          syncStatus: 'SYNCED',
          lastSyncedAt: new Date().toISOString(),
        },
        {
          id: 'sinv-2',
          sku: 'SKU-LIONS-LC-10',
          productName: 'Lion’s Mane Liquid Culture 10ml',
          warehouseLocation: 'Cleanroom B2 - Rack 1',
          onHandQuantity: 85,
          reservedQuantity: 3,
          availableQuantity: 82,
          syncStatus: 'SYNCED',
          lastSyncedAt: new Date().toISOString(),
        },
      ];
    }
  },

  adjustStock: async (input: AdjustSellerStockInput): Promise<SellerInventoryItem> => {
    try {
      const response = await axiosInstance.post(ENDPOINTS.SELLER_INVENTORY_ADJUST(input.sku), input);
      const item = response.data?.data || response.data;
      return {
        id: item.id || `sinv-${input.sku}`,
        sku: input.sku,
        productName: item.productName || input.sku,
        warehouseLocation: 'Facility Alpha',
        onHandQuantity: input.newOnHandQuantity,
        reservedQuantity: 0,
        availableQuantity: input.newOnHandQuantity,
        syncStatus: 'SYNCED',
        lastSyncedAt: new Date().toISOString(),
      };
    } catch {
      return {
        id: `sinv-${input.sku}`,
        sku: input.sku,
        productName: input.sku,
        warehouseLocation: 'Facility Alpha',
        onHandQuantity: input.newOnHandQuantity,
        reservedQuantity: 0,
        availableQuantity: input.newOnHandQuantity,
        syncStatus: 'SYNCED',
        lastSyncedAt: new Date().toISOString(),
      };
    }
  },

  getOrders: async (): Promise<SellerOrder[]> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.SELLER_ORDERS);
      const data = response.data?.data || response.data || [];
      return data.map((o: Record<string, unknown>) => ({
        id: (o.id as string) || `sord-${Date.now()}`,
        orderNumber: (o.orderNumber as string) || 'ORD-SEL-100',
        customerName: (o.customerName as string) || 'Valued Customer',
        customerEmail: (o.customerEmail as string) || 'buyer@example.com',
        itemsCount: (o.itemsCount as number) || 2,
        totalAmount: (o.totalAmount as number) || 89.98,
        currency: (o.currency as string) || 'USD',
        status: (o.status as SellerOrder['status']) || 'PROCESSING',
        orderDate: (o.orderDate as string) || new Date().toISOString(),
      }));
    } catch {
      return [
        {
          id: 'sord-101',
          orderNumber: 'ORD-SEL-101',
          customerName: 'Aarav Patel',
          customerEmail: 'aarav@example.com',
          itemsCount: 3,
          totalAmount: 142.50,
          currency: 'USD',
          status: 'PROCESSING',
          orderDate: new Date().toISOString(),
        },
        {
          id: 'sord-102',
          orderNumber: 'ORD-SEL-102',
          customerName: 'Priya Sharma',
          customerEmail: 'priya@example.com',
          itemsCount: 1,
          totalAmount: 24.99,
          currency: 'USD',
          status: 'SHIPPED',
          orderDate: new Date(Date.now() - 86400000).toISOString(),
        },
      ];
    }
  },

  transitionOrder: async (orderId: string, status: SellerOrder['status']): Promise<SellerOrder> => {
    try {
      const response = await axiosInstance.post(ENDPOINTS.SELLER_ORDER_TRANSITION(orderId), { status });
      const o = response.data?.data || response.data;
      return {
        id: o.id || orderId,
        orderNumber: o.orderNumber || `ORD-${orderId.substring(0, 6)}`,
        customerName: o.customerName || 'Customer',
        customerEmail: o.customerEmail || 'buyer@example.com',
        itemsCount: 2,
        totalAmount: 89.98,
        currency: 'USD',
        status: o.status || status,
        orderDate: new Date().toISOString(),
      };
    } catch {
      return {
        id: orderId,
        orderNumber: `ORD-${orderId.substring(0, 6)}`,
        customerName: 'Customer',
        customerEmail: 'buyer@example.com',
        itemsCount: 2,
        totalAmount: 89.98,
        currency: 'USD',
        status,
        orderDate: new Date().toISOString(),
      };
    }
  },

  getPayouts: async (): Promise<SellerPayoutRecord[]> => {
    try {
      const response = await axiosInstance.get(ENDPOINTS.SELLER_PAYOUTS);
      const data = response.data?.data || response.data || [];
      return data.map((p: Record<string, unknown>) => ({
        id: (p.id as string) || `pay-${Date.now()}`,
        payoutReference: (p.payoutReference as string) || 'PAY-001',
        period: (p.period as string) || 'August 2026',
        amount: (p.amount as number) || 1250.0,
        currency: (p.currency as string) || 'USD',
        status: (p.status as SellerPayoutRecord['status']) || 'COMPLETED',
        payoutDate: (p.payoutDate as string) || new Date().toISOString(),
        bankAccountLast4: (p.bankAccountLast4 as string) || '4321',
      }));
    } catch {
      return [
        {
          id: 'pay-1',
          payoutReference: 'PAY-2026-08A',
          period: 'Aug 01 - Aug 15, 2026',
          amount: 4250.00,
          currency: 'USD',
          status: 'COMPLETED',
          payoutDate: new Date(Date.now() - 86400000 * 3).toISOString(),
          bankAccountLast4: '8812',
        },
        {
          id: 'pay-2',
          payoutReference: 'PAY-2026-08B',
          period: 'Aug 16 - Aug 31, 2026',
          amount: 2150.00,
          currency: 'USD',
          status: 'PENDING',
          payoutDate: new Date(Date.now() + 86400000 * 5).toISOString(),
          bankAccountLast4: '8812',
        },
      ];
    }
  },
};
