export type SellerStatus = 'ACTIVE' | 'PENDING' | 'SUSPENDED';

export type InventorySyncStatus = 'SYNCED' | 'SYNCING' | 'SYNC_ERROR' | 'OUT_OF_SYNC';

export interface SellerMetrics {
  totalSales: number;
  monthlyRevenue: number;
  activeListingsCount: number;
  totalInventoryOnHand: number;
  syncErrorCount: number;
  pendingOrdersCount: number;
  fulfilledOrdersCount: number;
  payoutPendingAmount: number;
  payoutSettledAmount: number;
}

export interface SellerProductItem {
  id: string;
  name: string;
  sku: string;
  category: string;
  price: number;
  strikeOutPrice?: number | null;
  currency: string;
  onHandQuantity: number;
  reservedQuantity: number;
  syncStatus: InventorySyncStatus;
  status: 'ACTIVE' | 'DRAFT' | 'ARCHIVED';
  updatedAt: string;
}

export interface SellerInventoryItem {
  id: string;
  sku: string;
  productName: string;
  warehouseLocation: string;
  onHandQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  syncStatus: InventorySyncStatus;
  lastSyncedAt: string;
}

export interface SellerOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  itemsCount: number;
  totalAmount: number;
  currency: string;
  status: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  orderDate: string;
}

export interface SellerPayoutRecord {
  id: string;
  payoutReference: string;
  period: string;
  amount: number;
  currency: string;
  status: 'COMPLETED' | 'PENDING' | 'PROCESSING';
  payoutDate: string;
  bankAccountLast4: string;
}

export interface CreateSellerProductInput {
  name: string;
  sku: string;
  category: string;
  price: number;
  strikeOutPrice?: number | null;
  initialStock: number;
}

export interface AdjustSellerStockInput {
  sku: string;
  newOnHandQuantity: number;
  reason: string;
}
