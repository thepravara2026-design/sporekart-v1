import { useQuery } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';

export const useGrowerDashboard = () => {
  const metricsQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.dashboard(),
    queryFn: growerApi.getDashboardMetrics,
  });

  const operationalQuery = useQuery({
    queryKey: [...GROWER_QUERY_KEYS.all, 'operational-status'],
    queryFn: growerApi.getOperationalStatus,
  });

  return {
    metrics: metricsQuery.data,
    operationalStatus: operationalQuery.data,
    isLoading: metricsQuery.isLoading || operationalQuery.isLoading,
    isError: metricsQuery.isError || operationalQuery.isError,
    error: metricsQuery.error || operationalQuery.error,
    refetch: () => {
      metricsQuery.refetch();
      operationalQuery.refetch();
    },
  };
};
