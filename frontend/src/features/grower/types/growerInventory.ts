export interface GrowerInventoryItem {
  id: string;
  sku: string;
  productName: string;
  onHandQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderPoint: number;
  status: 'HEALTHY' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'OVERSTOCKED';
  updatedAt: string;
}

export interface StockAdjustmentPayload {
  sku: string;
  newOnHandQuantity: number;
  reason: string;
}

export interface DamagedStockPayload {
  sku: string;
  quantity: number;
  reason?: string;
}

export interface StockMovementRecord {
  id: string;
  sku: string;
  type: 'ADJUSTMENT' | 'RESERVATION' | 'RELEASE' | 'DAMAGED' | 'FULFILLMENT';
  quantityChange: number;
  previousQuantity: number;
  newQuantity: number;
  reason: string;
  timestamp: string;
}
