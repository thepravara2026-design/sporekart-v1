import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { sellerApi } from '../api/sellerApi';
import { AdjustSellerStockInput, CreateSellerProductInput, SellerOrder } from '../types/seller';

export const SELLER_QUERY_KEYS = {
  METRICS: ['seller', 'metrics'],
  PRODUCTS: ['seller', 'products'],
  INVENTORY: ['seller', 'inventory'],
  ORDERS: ['seller', 'orders'],
  PAYOUTS: ['seller', 'payouts'],
} as const;

export function useSellerMetrics() {
  return useQuery({
    queryKey: SELLER_QUERY_KEYS.METRICS,
    queryFn: sellerApi.getMetrics,
    staleTime: 60000,
  });
}

export function useSellerProducts() {
  return useQuery({
    queryKey: SELLER_QUERY_KEYS.PRODUCTS,
    queryFn: sellerApi.getProducts,
    staleTime: 30000,
  });
}

export function useCreateSellerProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateSellerProductInput) => sellerApi.createProduct(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SELLER_QUERY_KEYS.PRODUCTS });
      queryClient.invalidateQueries({ queryKey: SELLER_QUERY_KEYS.METRICS });
    },
  });
}

export function useSellerInventory() {
  return useQuery({
    queryKey: SELLER_QUERY_KEYS.INVENTORY,
    queryFn: sellerApi.getInventory,
    staleTime: 30000,
  });
}

export function useAdjustSellerStock() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: AdjustSellerStockInput) => sellerApi.adjustStock(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SELLER_QUERY_KEYS.INVENTORY });
      queryClient.invalidateQueries({ queryKey: SELLER_QUERY_KEYS.PRODUCTS });
    },
  });
}

export function useSellerOrders() {
  return useQuery({
    queryKey: SELLER_QUERY_KEYS.ORDERS,
    queryFn: sellerApi.getOrders,
    staleTime: 15000,
  });
}

export function useTransitionSellerOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ orderId, status }: { orderId: string; status: SellerOrder['status'] }) =>
      sellerApi.transitionOrder(orderId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SELLER_QUERY_KEYS.ORDERS });
      queryClient.invalidateQueries({ queryKey: SELLER_QUERY_KEYS.METRICS });
    },
  });
}

export function useSellerPayouts() {
  return useQuery({
    queryKey: SELLER_QUERY_KEYS.PAYOUTS,
    queryFn: sellerApi.getPayouts,
    staleTime: 60000,
  });
}
