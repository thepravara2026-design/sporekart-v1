import { useQuery } from '@tanstack/react-query';
import { authApi, UserProfileDto } from '../../../services/authApi';

/** Server state key for the authenticated customer profile. */
export const PROFILE_KEYS = {
  all: ['profile'] as const,
  me: ['profile', 'me'] as const,
} as const;

export const useCustomerProfile = () => {
  return useQuery({
    queryKey: PROFILE_KEYS.me,
    queryFn: (): Promise<UserProfileDto> => authApi.getCurrentUser(),
    staleTime: 1000 * 60 * 5,
  });
};
