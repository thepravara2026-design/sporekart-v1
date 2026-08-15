import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';

export interface StockAvailabilityDto {
  sku: string;
  status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  available: boolean;
  lowStockThreshold: number;
}

export interface InventoryItemDto {
  id: string;
  productId: string;
  variantId?: string;
  sku: string;
  onHandQuantity: number;
  reservedQuantity: number;
  damagedQuantity: number;
  availableQuantity: number;
  lowStockThreshold: number;
  isLowStock: boolean;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface StockMovementDto {
  id: string;
  inventoryItemId: string;
  movementType: string;
  quantity: number;
  referenceType?: string;
  referenceId?: string;
  previousOnHand: number;
  resultingOnHand: number;
  previousReserved: number;
  resultingReserved: number;
  createdAt: string;
}

export const inventoryApi = {
  getAvailability: async (sku: string): Promise<StockAvailabilityDto> => {
    const response = await axiosInstance.get<{ data: StockAvailabilityDto }>(ENDPOINTS.INVENTORY_AVAILABILITY(sku));
    return response.data.data;
  },

  listAdminInventory: async (): Promise<InventoryItemDto[]> => {
    const response = await axiosInstance.get<{ data: InventoryItemDto[] }>(ENDPOINTS.ADMIN_INVENTORY_LIST);
    return response.data.data;
  },

  getAdminInventoryBySku: async (sku: string): Promise<InventoryItemDto> => {
    const response = await axiosInstance.get<{ data: InventoryItemDto }>(ENDPOINTS.ADMIN_INVENTORY_BY_SKU(sku));
    return response.data.data;
  },

  listMovements: async (sku: string): Promise<StockMovementDto[]> => {
    const response = await axiosInstance.get<{ data: StockMovementDto[] }>(ENDPOINTS.ADMIN_INVENTORY_MOVEMENTS(sku));
    return response.data.data;
  },

  adjustStock: async (sku: string, newOnHandQuantity: number, reason?: string): Promise<InventoryItemDto> => {
    const response = await axiosInstance.post<{ data: InventoryItemDto }>(ENDPOINTS.ADMIN_INVENTORY_ADJUST(sku), {
      sku,
      newOnHandQuantity,
      reason: reason ?? 'ADMIN_MANUAL_ADJUSTMENT'
    });
    return response.data.data;
  },

  recordDamagedStock: async (sku: string, quantity: number, reason?: string): Promise<InventoryItemDto> => {
    const response = await axiosInstance.post<{ data: InventoryItemDto }>(
      `${ENDPOINTS.ADMIN_INVENTORY_DAMAGED(sku)}?quantity=${quantity}&reason=${encodeURIComponent(reason ?? 'DEFECT_QA')}`
    );
    return response.data.data;
  }
};
