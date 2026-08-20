import { useQuery } from '@tanstack/react-query';
import { cartApi } from '../../../services/cartApi';

/**
 * Single source of truth for cart cache keys. Every cart mutation and the
 * header cart count read/write through this key so they never diverge.
 */
export const CART_KEYS = {
  all: ['cart'] as const,
} as const;

export const useCart = () => {
  return useQuery({
    queryKey: CART_KEYS.all,
    queryFn: () => cartApi.getCart(),
    staleTime: 0,
  });
};
