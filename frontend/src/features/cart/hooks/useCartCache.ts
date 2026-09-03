import { QueryClient } from '@tanstack/react-query';
import { ApiResponse } from '../../../types/api';
import { CartDto } from '../../../services/cartApi';
import { CART_KEYS } from './useCart';

/**
 * Applies a server-confirmed cart response to the shared cart cache.
 * `setQueryData` gives immediate UI consistency; `invalidateQueries` marks the
 * query stale so subscribers (e.g. the header count) reconcile with the
 * backend. The backend response is the single source of truth for totals.
 */
export const syncCartCache = (queryClient: QueryClient, response: ApiResponse<CartDto>): void => {
  queryClient.setQueryData<ApiResponse<CartDto>>(CART_KEYS.all, response);
  queryClient.invalidateQueries({ queryKey: CART_KEYS.all });
};
