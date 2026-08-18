export const DEFAULT_PAGE_SIZE = 12;

export const DEFAULT_SORT = 'createdAt,desc';

/**
 * Matches the backend `cart.max-item-quantity` default. Quantity selection is
 * UX-only; the backend remains authoritative.
 */
export const DEFAULT_MAX_PRODUCT_QUANTITY = 50;

export const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Newest Additions' },
  { value: 'name,asc', label: 'Name (A to Z)' },
  { value: 'name,desc', label: 'Name (Z to A)' },
  { value: 'price,asc', label: 'Price (Low to High)' },
  { value: 'price,desc', label: 'Price (High to Low)' },
];

export const DEFAULT_CATALOG_FILTERS = {
  search: '',
  categoryId: '',
  status: '' as const,
  minPrice: undefined,
  maxPrice: undefined,
  sort: DEFAULT_SORT,
  page: 0,
  size: DEFAULT_PAGE_SIZE,
};
