import { useCart } from './useCart';

/**
 * Header cart count backed by the shared `['cart']` query cache — the single
 * source of truth. Updates automatically whenever any cart mutation writes or
 * invalidates the cart cache (add, quantity update, remove, clear).
 */
export const useCartCount = (): number => {
  const { data } = useCart();
  return data?.data?.itemCount ?? 0;
};