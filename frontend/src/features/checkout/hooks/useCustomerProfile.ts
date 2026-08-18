import { useQuery } from '@tanstack/react-query';
import { authApi, UserProfileDto } from '../../../services/authApi';
import { isAuthenticated } from '../../cart/utils/cartUtils';

/** Server state key for the authenticated customer profile. */
export const PROFILE_KEYS = {
  all: ['profile'] as const,
  me: ['profile', 'me'] as const,
} as const;

/**
 * Current authenticated customer profile (from the backend `/auth/me`). Used
 * to prefill checkout customer information; the backend remains the only
 * source of identity data.
 */
export const useCustomerProfile = () => {
  return useQuery({
    queryKey: PROFILE_KEYS.me,
    queryFn: (): Promise<UserProfileDto> => authApi.getCurrentUser(),
    enabled: isAuthenticated(),
    staleTime: 1000 * 60 * 5,
  });
};
