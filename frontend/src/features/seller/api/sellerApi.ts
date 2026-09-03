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
    const response = await axiosInstance.get(ENDPOINTS.SELLER_DASHBOARD);
    const data = response.data?.data || response.data;
    return {
      totalSales: data.totalSales ?? 0,
      monthlyRevenue: data.monthlyRevenue ?? 0,
      activeListingsCount: data.activeListingsCount ?? 0,
      totalInventoryOnHand: data.totalInventoryOnHand ?? 0,
      syncErrorCount: data.syncErrorCount ?? 0,
      pendingOrdersCount: data.pendingOrdersCount ?? 0,
      fulfilledOrdersCount: data.fulfilledOrdersCount ?? 0,
      payoutPendingAmount: data.payoutPendingAmount ?? 0,
      payoutSettledAmount: data.payoutSettledAmount ?? 0,
    };
  },

  getProducts: async (): Promise<SellerProductItem[]> => {
    const response = await axiosInstance.get(ENDPOINTS.SELLER_PRODUCTS);
    const data = response.data?.data || response.data || [];
    return data.map((p: Record<string, unknown>) => ({
      id: (p.id as string) || '',
      name: (p.name as string) || '',
      sku: (p.sku as string) || '',
      category: (p.category as string) || '',
      price: Number(p.price) || 0,
      strikeOutPrice: p.strikeOutPrice ? Number(p.strikeOutPrice) : null,
      currency: (p.currency as string) || 'INR',
      onHandQuantity: Number(p.onHandQuantity) || 0,
      reservedQuantity: Number(p.reservedQuantity) || 0,
      syncStatus: (p.syncStatus as SellerProductItem['syncStatus']) || 'SYNCED',
      status: (p.status as SellerProductItem['status']) || 'ACTIVE',
      updatedAt: (p.updatedAt as string) || new Date().toISOString(),
    }));
  },

  createProduct: async (input: CreateSellerProductInput): Promise<SellerProductItem> => {
    const response = await axiosInstance.post(ENDPOINTS.SELLER_PRODUCTS, input);
    const data = response.data?.data || response.data;
    return {
      id: data.id || '',
      name: data.name || input.name,
      sku: data.sku || input.sku,
      category: input.category,
      price: input.price,
      strikeOutPrice: input.strikeOutPrice || null,
      currency: 'INR',
      onHandQuantity: input.initialStock,
      reservedQuantity: 0,
      syncStatus: 'SYNCED',
      status: 'ACTIVE',
      updatedAt: new Date().toISOString(),
    };
  },

  getInventory: async (): Promise<SellerInventoryItem[]> => {
    const response = await axiosInstance.get(ENDPOINTS.SELLER_INVENTORY);
    const data = response.data?.data || response.data || [];
    return data.map((item: Record<string, unknown>) => ({
      id: (item.id as string) || '',
      sku: (item.sku as string) || '',
      productName: (item.productName as string) || '',
      warehouseLocation: (item.warehouseLocation as string) || '',
      onHandQuantity: Number(item.onHandQuantity) || 0,
      reservedQuantity: Number(item.reservedQuantity) || 0,
      availableQuantity: Number(item.availableQuantity) || 0,
      syncStatus: (item.syncStatus as SellerInventoryItem['syncStatus']) || 'SYNCED',
      lastSyncedAt: (item.lastSyncedAt as string) || new Date().toISOString(),
    }));
  },

  adjustStock: async (input: AdjustSellerStockInput): Promise<SellerInventoryItem> => {
    const response = await axiosInstance.post(ENDPOINTS.SELLER_INVENTORY_ADJUST(input.sku), input);
    const item = response.data?.data || response.data;
    return {
      id: item.id || '',
      sku: input.sku,
      productName: item.productName || input.sku,
      warehouseLocation: item.warehouseLocation || '',
      onHandQuantity: input.newOnHandQuantity,
      reservedQuantity: item.reservedQuantity || 0,
      availableQuantity: input.newOnHandQuantity - (item.reservedQuantity || 0),
      syncStatus: 'SYNCED',
      lastSyncedAt: new Date().toISOString(),
    };
  },

  getOrders: async (): Promise<SellerOrder[]> => {
    const response = await axiosInstance.get(ENDPOINTS.SELLER_ORDERS);
    const data = response.data?.data || response.data || [];
    return data.map((o: Record<string, unknown>) => ({
      id: (o.id as string) || '',
      orderNumber: (o.orderNumber as string) || '',
      customerName: (o.customerName as string) || '',
      customerEmail: (o.customerEmail as string) || '',
      itemsCount: Number(o.itemsCount) || 0,
      totalAmount: Number(o.totalAmount) || 0,
      currency: (o.currency as string) || 'INR',
      status: (o.status as SellerOrder['status']) || 'PROCESSING',
      orderDate: (o.orderDate as string) || new Date().toISOString(),
    }));
  },

  transitionOrder: async (orderId: string, status: SellerOrder['status']): Promise<SellerOrder> => {
    const response = await axiosInstance.post(ENDPOINTS.SELLER_ORDER_TRANSITION(orderId), { status });
    const o = response.data?.data || response.data;
    return {
      id: o.id || orderId,
      orderNumber: o.orderNumber || '',
      customerName: o.customerName || '',
      customerEmail: o.customerEmail || '',
      itemsCount: Number(o.itemsCount) || 0,
      totalAmount: Number(o.totalAmount) || 0,
      currency: (o.currency as string) || 'INR',
      status: (o.status as SellerOrder['status']) || status,
      orderDate: (o.orderDate as string) || new Date().toISOString(),
    };
  },

  getPayouts: async (): Promise<SellerPayoutRecord[]> => {
    const response = await axiosInstance.get(ENDPOINTS.SELLER_PAYOUTS);
    const data = response.data?.data || response.data || [];
    return data.map((p: Record<string, unknown>) => ({
      id: (p.id as string) || '',
      payoutReference: (p.payoutReference as string) || '',
      period: (p.period as string) || '',
      amount: Number(p.amount) || 0,
      currency: (p.currency as string) || 'INR',
      status: (p.status as SellerPayoutRecord['status']) || 'COMPLETED',
      payoutDate: (p.payoutDate as string) || new Date().toISOString(),
      bankAccountLast4: (p.bankAccountLast4 as string) || '',
    }));
  },
};
