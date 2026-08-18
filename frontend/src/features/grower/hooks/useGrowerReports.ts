import { useQuery } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';

export const useGrowerReports = (period = '30d') => {
  const summaryQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.reports(period),
    queryFn: growerApi.getReportSummary,
  });

  return {
    reportSummary: summaryQuery.data,
    isLoading: summaryQuery.isLoading,
    isError: summaryQuery.isError,
    error: summaryQuery.error,
    refetch: summaryQuery.refetch,
  };
};
