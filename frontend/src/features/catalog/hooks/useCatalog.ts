import { useQuery } from '@tanstack/react-query';
import { catalogApi } from '../../../services/catalogApi';
import { ProductQueryParams, CategoryQueryParams } from '../../../types/catalog';

export const CATALOG_KEYS = {
  allProducts: ['catalog', 'products'] as const,
  productsList: (params?: ProductQueryParams) => ['catalog', 'products', params] as const,
  productDetail: (id: string) => ['catalog', 'products', 'detail', id] as const,
  allCategories: ['catalog', 'categories'] as const,
  categoriesList: (params?: CategoryQueryParams) => ['catalog', 'categories', params] as const,
  categoryDetail: (id: string) => ['catalog', 'categories', 'detail', id] as const,
};

export function useProducts(params?: ProductQueryParams) {
  return useQuery({
    queryKey: CATALOG_KEYS.productsList(params),
    queryFn: ({ signal }) => catalogApi.getProducts(params, signal),
  });
}

export function useProduct(productId: string) {
  return useQuery({
    queryKey: CATALOG_KEYS.productDetail(productId),
    queryFn: ({ signal }) => catalogApi.getProduct(productId, signal),
    enabled: Boolean(productId),
  });
}

export function useCategories(params?: CategoryQueryParams) {
  return useQuery({
    queryKey: CATALOG_KEYS.categoriesList(params),
    queryFn: ({ signal }) => catalogApi.getCategories(params, signal),
  });
}

export function useCategory(categoryId: string) {
  return useQuery({
    queryKey: CATALOG_KEYS.categoryDetail(categoryId),
    queryFn: ({ signal }) => catalogApi.getCategory(categoryId, signal),
    enabled: Boolean(categoryId),
  });
}
