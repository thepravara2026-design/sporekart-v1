import { ProductStatus } from '../types/catalog';
import { BadgeVariant } from '../../../components/ui/Badge';

/**
 * Backend-authorized purchasability check: only products in ACTIVE status can
 * be added to the cart (mirrors the backend CartPort/CatalogAdapter rule).
 */
export const isProductPurchasable = (status: ProductStatus): boolean => status === 'ACTIVE';

export const formatPrice = (amount: number, currency: string = 'INR'): string => {
  try {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency.toUpperCase(),
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `₹${amount.toFixed(2)}`;
  }
};

export type StockAvailabilityState = 'NORMAL' | 'LIMITED' | 'ORDER_NOW' | 'OUT_OF_STOCK';

export interface StockAvailabilityInfo {
  state: StockAvailabilityState;
  label: string;
  badgeVariant: BadgeVariant;
  isPurchasable: boolean;
}

export const getStockAvailabilityInfo = (
  status: ProductStatus,
  availableStock?: number | null
): StockAvailabilityInfo => {
  if (status === 'OUT_OF_STOCK' || status === 'DISCONTINUED' || status === 'ARCHIVED') {
    return {
      state: 'OUT_OF_STOCK',
      label: status === 'DISCONTINUED' ? 'Discontinued' : 'Out of Stock',
      badgeVariant: 'danger',
      isPurchasable: false,
    };
  }

  if (typeof availableStock === 'number') {
    if (availableStock <= 0) {
      return {
        state: 'OUT_OF_STOCK',
        label: 'Out of Stock',
        badgeVariant: 'danger',
        isPurchasable: false,
      };
    }
    if (availableStock < 5) {
      return {
        state: 'ORDER_NOW',
        label: 'Order Now',
        badgeVariant: 'warning',
        isPurchasable: status === 'ACTIVE',
      };
    }
    if (availableStock < 10) {
      return {
        state: 'LIMITED',
        label: 'Limited Stock',
        badgeVariant: 'warning',
        isPurchasable: status === 'ACTIVE',
      };
    }
    return {
      state: 'NORMAL',
      label: 'In Stock',
      badgeVariant: 'success',
      isPurchasable: status === 'ACTIVE',
    };
  }

  return {
    state: status === 'ACTIVE' ? 'NORMAL' : 'OUT_OF_STOCK',
    label: getStatusLabel(status),
    badgeVariant: getStatusBadgeVariant(status),
    isPurchasable: status === 'ACTIVE',
  };
};

export const getStatusBadgeVariant = (status: ProductStatus): BadgeVariant => {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'OUT_OF_STOCK':
      return 'danger';
    case 'DRAFT':
      return 'neutral';
    case 'DISCONTINUED':
    case 'ARCHIVED':
      return 'warning';
    default:
      return 'default';
  }
};

export const getStatusLabel = (status: ProductStatus): string => {
  switch (status) {
    case 'ACTIVE':
      return 'In Stock';
    case 'OUT_OF_STOCK':
      return 'Out of Stock';
    case 'DRAFT':
      return 'Draft';
    case 'DISCONTINUED':
      return 'Discontinued';
    case 'ARCHIVED':
      return 'Archived';
    default:
      return status;
  }
};
