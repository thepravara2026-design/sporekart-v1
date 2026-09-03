import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';
import { StockAdjustmentPayload } from '../types/growerInventory';

export const useGrowerInventory = () => {
  const queryClient = useQueryClient();

  const inventoryQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.inventory(),
    queryFn: growerApi.getInventory,
  });

  const adjustStockMutation = useMutation({
    mutationFn: (payload: StockAdjustmentPayload) => growerApi.adjustStock(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.inventory() });
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.dashboard() });
    },
  });

  return {
    inventory: inventoryQuery.data || [],
    isLoading: inventoryQuery.isLoading,
    isError: inventoryQuery.isError,
    error: inventoryQuery.error,
    refetch: inventoryQuery.refetch,
    adjustStock: adjustStockMutation.mutateAsync,
    isAdjusting: adjustStockMutation.isPending,
    adjustError: adjustStockMutation.error,
  };
};
