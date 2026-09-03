export type ProductStatus = 'DRAFT' | 'ACTIVE' | 'OUT_OF_STOCK' | 'DISCONTINUED' | 'ARCHIVED';
export type CategoryStatus = 'ACTIVE' | 'INACTIVE';
export type QuantityUnit = 'G' | 'KG' | 'ML' | 'L';

export interface ProductVariant {
  id: string;
  productId: string;
  sku: string;
  quantityValue: number;
  quantityUnit: QuantityUnit;
  formattedQuantity: string;
  sellingPrice: number;
  strikeOutPrice?: number | null;
  status: ProductStatus;
  availableQuantity?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductImage {
  id: string;
  imageUrl: string;
  displayOrder: number;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  status: CategoryStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string | null;
  price: number;
  strikeOutPrice?: number | null;
  currency: string;
  status: ProductStatus;
  category: Category | null;
  variants?: ProductVariant[];
  imageUrl?: string | null;
  images?: ProductImage[];
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ProductQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  categoryId?: string;
  status?: ProductStatus;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
}

export interface CategoryQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: CategoryStatus;
  search?: string;
}
