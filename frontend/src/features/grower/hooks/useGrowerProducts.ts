import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';
import { CreateGrowerProductInput, GrowerProductFilter } from '../types/growerProduct';

export const useGrowerProducts = (filters?: GrowerProductFilter) => {
  const queryClient = useQueryClient();

  const productsQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.products(filters),
    queryFn: () => growerApi.getProducts(filters),
  });

  const createProductMutation = useMutation({
    mutationFn: (input: CreateGrowerProductInput) => growerApi.createProduct(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.all });
    },
  });

  return {
    products: productsQuery.data?.items || [],
    totalCount: productsQuery.data?.total || 0,
    isLoading: productsQuery.isLoading,
    isError: productsQuery.isError,
    error: productsQuery.error,
    refetch: productsQuery.refetch,
    createProduct: createProductMutation.mutateAsync,
    isCreating: createProductMutation.isPending,
  };
};
