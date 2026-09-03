import { useQuery } from '@tanstack/react-query';
import { returnApi, ReturnEligibilityDto } from '../../../services/returnApi';
import { ORDER_KEYS } from './useOrder';

/**
 * Return eligibility for an order. The backend is authoritative: the order
 * must be delivered and within the return window. Used only to decide whether
 * to surface the "Request Return" handoff action.
 */
export const useReturnEligibility = (reference: string, enabled = true) => {
  return useQuery({
    queryKey: ORDER_KEYS.returnEligibility(reference),
    queryFn: (): Promise<ReturnEligibilityDto> => returnApi.checkEligibility(reference),
    enabled: Boolean(reference) && enabled,
    retry: false,
  });
};
