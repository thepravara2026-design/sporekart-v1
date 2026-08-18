import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';
import { GrowerSettings } from '../types/grower';

export const useGrowerSettings = () => {
  const queryClient = useQueryClient();

  const settingsQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.settings(),
    queryFn: growerApi.getSettings,
  });

  const updateSettingsMutation = useMutation({
    mutationFn: (payload: Partial<GrowerSettings>) => growerApi.updateSettings(payload),
    onSuccess: (updated) => {
      queryClient.setQueryData(GROWER_QUERY_KEYS.settings(), updated);
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.settings() });
    },
  });

  return {
    settings: settingsQuery.data,
    isLoading: settingsQuery.isLoading,
    isError: settingsQuery.isError,
    error: settingsQuery.error,
    updateSettings: updateSettingsMutation.mutateAsync,
    isUpdating: updateSettingsMutation.isPending,
  };
};
