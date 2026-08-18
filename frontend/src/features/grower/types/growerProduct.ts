export type GrowerProductStatus = 'ACTIVE' | 'INACTIVE' | 'DRAFT' | 'ARCHIVED';

export interface GrowerProduct {
  id: string;
  name: string;
  sku: string;
  description?: string;
  price: number;
  currency: string;
  status: GrowerProductStatus;
  categoryId?: string;
  categoryName?: string;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface GrowerProductFilter {
  page?: number;
  size?: number;
  search?: string;
  status?: GrowerProductStatus;
  categoryId?: string;
  sort?: string;
  [key: string]: unknown;
}

export interface CreateGrowerProductInput {
  name: string;
  sku: string;
  description?: string;
  price: number;
  categoryId?: string;
  status?: GrowerProductStatus;
  imageUrl?: string;
}
