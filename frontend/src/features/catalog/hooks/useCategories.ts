import { useQuery } from '@tanstack/react-query';
import { catalogApi } from '../api/catalogApi';
import { CategoryQueryParams } from '../types/catalog';

export const useCategories = (params?: CategoryQueryParams) => {
  return useQuery({
    queryKey: ['catalog', 'categories', params],
    queryFn: ({ signal }) => catalogApi.getCategories(params, signal),
  });
};
