import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';
import { OrderTransitionPayload } from '../types/growerOrder';

export const useGrowerOrders = () => {
  const queryClient = useQueryClient();

  const ordersQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.orders(),
    queryFn: growerApi.getOrders,
  });

  const transitionOrderMutation = useMutation({
    mutationFn: (payload: OrderTransitionPayload) => growerApi.transitionOrder(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.orders() });
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.dashboard() });
    },
  });

  return {
    orders: ordersQuery.data || [],
    isLoading: ordersQuery.isLoading,
    isError: ordersQuery.isError,
    error: ordersQuery.error,
    refetch: ordersQuery.refetch,
    transitionOrder: transitionOrderMutation.mutateAsync,
    isTransitioning: transitionOrderMutation.isPending,
  };
};
