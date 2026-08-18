import { ProductStatus } from '../types/catalog';
import { BadgeVariant } from '../../../components/ui/Badge';

export const formatPrice = (amount: number, currency: string = 'USD'): string => {
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency.toUpperCase(),
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `$${amount.toFixed(2)}`;
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
