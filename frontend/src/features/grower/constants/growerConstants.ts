export const GROWER_QUERY_KEYS = {
  all: ['grower'] as const,
  dashboard: () => [...GROWER_QUERY_KEYS.all, 'dashboard'] as const,
  profile: () => [...GROWER_QUERY_KEYS.all, 'profile'] as const,
  products: (filters?: Record<string, unknown>) => [...GROWER_QUERY_KEYS.all, 'products', filters] as const,
  inventory: (sku?: string) => [...GROWER_QUERY_KEYS.all, 'inventory', sku] as const,
  orders: (filters?: Record<string, unknown>) => [...GROWER_QUERY_KEYS.all, 'orders', filters] as const,
  shipments: (filters?: Record<string, unknown>) => [...GROWER_QUERY_KEYS.all, 'shipments', filters] as const,
  reports: (period?: string) => [...GROWER_QUERY_KEYS.all, 'reports', period] as const,
  settings: () => [...GROWER_QUERY_KEYS.all, 'settings'] as const,
};

export const PRODUCT_STATUS_CONFIG = {
  ACTIVE: { label: 'Active', variant: 'success' as const },
  INACTIVE: { label: 'Inactive', variant: 'secondary' as const },
  DRAFT: { label: 'Draft', variant: 'warning' as const },
  ARCHIVED: { label: 'Archived', variant: 'danger' as const },
};

export const INVENTORY_STATUS_CONFIG = {
  HEALTHY: { label: 'Healthy', variant: 'success' as const },
  LOW_STOCK: { label: 'Low Stock', variant: 'warning' as const },
  OUT_OF_STOCK: { label: 'Out of Stock', variant: 'danger' as const },
  OVERSTOCKED: { label: 'Overstocked', variant: 'info' as const },
};

export const ORDER_STATUS_CONFIG = {
  PENDING_PAYMENT: { label: 'Pending Payment', variant: 'warning' as const },
  PAYMENT_CONFIRMED: { label: 'Payment Confirmed', variant: 'info' as const },
  PROCESSING: { label: 'Processing', variant: 'info' as const },
  READY_FOR_FULFILMENT: { label: 'Ready for Fulfillment', variant: 'primary' as const },
  SHIPPED: { label: 'Shipped', variant: 'primary' as const },
  OUT_FOR_DELIVERY: { label: 'Out for Delivery', variant: 'primary' as const },
  DELIVERED: { label: 'Delivered', variant: 'success' as const },
  COMPLETED: { label: 'Completed', variant: 'success' as const },
  CANCELLED: { label: 'Cancelled', variant: 'danger' as const },
};
