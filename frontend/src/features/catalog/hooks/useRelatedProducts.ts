import { useQuery } from '@tanstack/react-query';
import { catalogApi } from '../api/catalogApi';

export interface UseRelatedProductsOptions {
  categoryId?: string;
  currentProductId?: string;
  limit?: number;
}

export const useRelatedProducts = ({
  categoryId,
  currentProductId,
  limit = 4,
}: UseRelatedProductsOptions) => {
  const query = useQuery({
    queryKey: ['catalog', 'related-products', categoryId, currentProductId],
    queryFn: ({ signal }) =>
      catalogApi.getProducts(
        {
          categoryId,
          size: limit + 2,
        },
        signal
      ),
    enabled: !!categoryId,
  });

  const rawProducts = query.data?.data?.content || [];
  const relatedProducts = rawProducts
    .filter((p) => p.id !== currentProductId)
    .slice(0, limit);

  return {
    ...query,
    relatedProducts,
  };
};
