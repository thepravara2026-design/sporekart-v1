import { useQuery } from '@tanstack/react-query';
import { catalogApi } from '../api/catalogApi';

export const useProduct = (productId?: string) => {
  return useQuery({
    queryKey: ['catalog', 'product', productId],
    queryFn: ({ signal }) => catalogApi.getProduct(productId!, signal),
    enabled: !!productId,
  });
};
