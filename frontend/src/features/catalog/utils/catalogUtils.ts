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
