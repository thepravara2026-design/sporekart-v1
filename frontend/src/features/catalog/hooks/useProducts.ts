import { useQuery } from '@tanstack/react-query';
import { catalogApi } from '../api/catalogApi';
import { ProductQueryParams } from '../types/catalog';

export const useProducts = (params?: ProductQueryParams) => {
  return useQuery({
    queryKey: ['catalog', 'products', params],
    queryFn: ({ signal }) => catalogApi.getProducts(params, signal),
  });
};
