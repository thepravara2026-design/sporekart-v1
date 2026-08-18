import {
  Product,
  Category,
  ProductStatus,
  CategoryStatus,
  ProductQueryParams,
  CategoryQueryParams,
} from '../../../types/catalog';
import { PageResponse, ApiResponse } from '../../../types/api';

export type {
  Product,
  Category,
  ProductStatus,
  CategoryStatus,
  ProductQueryParams,
  CategoryQueryParams,
  PageResponse,
  ApiResponse,
};

export interface CatalogFilterState {
  search: string;
  categoryId: string;
  status: ProductStatus | '';
  minPrice?: number;
  maxPrice?: number;
  sort: string;
  page: number;
  size: number;
}
