export const ENDPOINTS = {
  HEALTH: '/api/v1/health',
  VERSION: '/api/v1/version',
  PRODUCTS: '/api/v1/catalog/products',
  PRODUCT_BY_ID: (id: string) => `/api/v1/catalog/products/${id}`,
  CATEGORIES: '/api/v1/catalog/categories',
  CATEGORY_BY_ID: (id: string) => `/api/v1/catalog/categories/${id}`,
} as const;
