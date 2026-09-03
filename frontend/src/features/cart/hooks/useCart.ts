import { useQuery } from '@tanstack/react-query';
import { cartApi } from '../../../services/cartApi';
import { isAuthenticated } from '../utils/cartUtils';

/**
 * Single source of truth for cart cache keys. Every cart mutation and the
 * header cart count read/write through this key so they never diverge.
 */
export const CART_KEYS = {
  all: ['cart'] as const,
} as const;

/**
 * Authenticated cart query. Disabled while no bearer token is present so the
 * backend is not spammed with 401 responses on public pages.
 */
export const useCart = () => {
  return useQuery({
    queryKey: CART_KEYS.all,
    queryFn: () => cartApi.getCart(),
    enabled: isAuthenticated(),
    staleTime: 0,
  });
};
