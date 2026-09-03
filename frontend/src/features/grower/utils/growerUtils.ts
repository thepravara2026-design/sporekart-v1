import { INVENTORY_STATUS_CONFIG, ORDER_STATUS_CONFIG, PRODUCT_STATUS_CONFIG } from '../constants/growerConstants';
import { GrowerProductStatus } from '../types/growerProduct';
import { GrowerOrderStatusType } from '../types/growerOrder';

export const formatCurrency = (amount: number, currency = 'INR'): string => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
  }).format(amount);
};

export const getProductStatusMeta = (status: GrowerProductStatus) => {
  return PRODUCT_STATUS_CONFIG[status] || { label: status, variant: 'secondary' as const };
};

export const getInventoryStatusMeta = (onHand: number, reserved: number, reorderPoint = 10) => {
  const available = Math.max(0, onHand - reserved);
  if (available <= 0) {
    return INVENTORY_STATUS_CONFIG.OUT_OF_STOCK;
  }
  if (available <= reorderPoint) {
    return INVENTORY_STATUS_CONFIG.LOW_STOCK;
  }
  return INVENTORY_STATUS_CONFIG.HEALTHY;
};

export const getOrderStatusMeta = (status: GrowerOrderStatusType) => {
  return ORDER_STATUS_CONFIG[status] || { label: status, variant: 'secondary' as const };
};
