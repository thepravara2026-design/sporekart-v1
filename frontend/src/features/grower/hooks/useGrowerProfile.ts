import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { growerApi } from '../api/growerApi';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';
import { GrowerProfile } from '../types/grower';

export const useGrowerProfile = () => {
  const queryClient = useQueryClient();

  const profileQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.profile(),
    queryFn: growerApi.getProfile,
  });

  const updateProfileMutation = useMutation({
    mutationFn: (payload: Partial<GrowerProfile>) => growerApi.updateProfile(payload),
    onSuccess: (updated) => {
      queryClient.setQueryData(GROWER_QUERY_KEYS.profile(), updated);
      queryClient.invalidateQueries({ queryKey: GROWER_QUERY_KEYS.profile() });
    },
  });

  return {
    profile: profileQuery.data,
    isLoading: profileQuery.isLoading,
    isError: profileQuery.isError,
    error: profileQuery.error,
    updateProfile: updateProfileMutation.mutateAsync,
    isUpdating: updateProfileMutation.isPending,
  };
};
