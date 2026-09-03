export type GrowerProductStatus = 'ACTIVE' | 'INACTIVE' | 'DRAFT' | 'ARCHIVED';

export interface GrowerProduct {
  id: string;
  name: string;
  sku: string;
  description?: string;
  price: number;
  strikeOutPrice?: number | null;
  currency: string;
  status: GrowerProductStatus;
  categoryId?: string;
  categoryName?: string;
  imageUrl?: string;
  images?: string[];
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
  strikeOutPrice?: number | null;
  categoryId?: string;
  status?: GrowerProductStatus;
  imageUrl?: string;
  imageUrls?: string[];
}
