export const DEFAULT_PAGE_SIZE = 12;

export const SORT_OPTIONS = [
  { value: 'name,asc', label: 'Name (A to Z)' },
  { value: 'name,desc', label: 'Name (Z to A)' },
  { value: 'price,asc', label: 'Price (Low to High)' },
  { value: 'price,desc', label: 'Price (High to Low)' },
  { value: 'createdAt,desc', label: 'Newest Additions' },
];

export const DEFAULT_CATALOG_FILTERS = {
  search: '',
  categoryId: '',
  status: '' as const,
  minPrice: undefined,
  maxPrice: undefined,
  sort: 'name,asc',
  page: 0,
  size: DEFAULT_PAGE_SIZE,
};
