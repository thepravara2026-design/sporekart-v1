import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { orderApi, OrderDto } from '../../../services/orderApi';
import { ApiResponse, PageResponse } from '../../../types/api';
import { OrderSummaryDto } from '../../../services/orderApi';
import { useToast } from '../../../components/ui/Toast';
import { getOrderErrorMessage } from '../utils/orderUtils';

export const ORDER_KEYS = {
  all: ['orders'] as const,
  list: (filters: { page: number; size: number; status?: string }) =>
    ['orders', 'list', filters] as const,
  detail: (reference: string) => ['orders', reference] as const,
  timeline: (reference: string) => ['orders', reference, 'timeline'] as const,
  shipment: (reference: string) => ['orders', reference, 'shipment'] as const,
  tracking: (reference: string) => ['orders', reference, 'tracking'] as const,
  returnEligibility: (reference: string) => ['orders', reference, 'return-eligibility'] as const,
} as const;

/** Load a single order by its reference (UUID or ORD-... string). */
export const useOrderByReference = (reference: string, enabled = true) => {
  return useQuery({
    queryKey: ORDER_KEYS.detail(reference),
    queryFn: () => orderApi.getOrderByReference(reference),
    enabled: Boolean(reference) && enabled,
  });
};

/** Paginated customer order history, ordered newest-first by the backend. */
export const useOrders = (page: number, size = 10, status?: string, enabled = true) => {
  return useQuery({
    queryKey: ORDER_KEYS.list({ page, size, status }),
    queryFn: (): Promise<ApiResponse<PageResponse<OrderSummaryDto>>> =>
      orderApi.getOrderHistory(page, size).then((response) => ({
        ...response,
        data: filterPage(response.data, status),
      })),
    enabled,
  });
};

/** Applies the optional status filter client-side over the paginated page. */
const filterPage = (page: PageResponse<OrderSummaryDto>, status?: string): PageResponse<OrderSummaryDto> => {
  if (!status || status === 'ALL') return page;
  const content = page.content.filter((order) => order.status === status);
  return {
    ...page,
    content,
    totalElements: content.length,
    totalPages: content.length > 0 ? 1 : 0,
  };
};

/** Order status event timeline for an order. */
export const useOrderTimeline = (reference: string, enabled = true) => {
  return useQuery({
    queryKey: ORDER_KEYS.timeline(reference),
    queryFn: () => orderApi.getOrderTimeline(reference),
    enabled: Boolean(reference) && enabled,
  });
};

/** Cancel an order. The backend rejects cancellations from non-cancellable states. */
export const useCancelOrder = () => {
  const queryClient = useQueryClient();
  const { addToast } = useToast();

  return useMutation({
    mutationFn: (input: { orderReference: string; reason?: string }): Promise<ApiResponse<OrderDto>> =>
      orderApi.cancelOrder(input.orderReference, input.reason),
    onSuccess: (response, input) => {
      addToast({
        variant: 'success',
        title: 'Order Cancelled',
        message: `Order ${response.data.orderNumber} was cancelled.`,
      });
      queryClient.invalidateQueries({ queryKey: ORDER_KEYS.all });
      queryClient.setQueryData(ORDER_KEYS.detail(input.orderReference), response);
    },
    onError: (error: Error) => {
      addToast({
        variant: 'error',
        title: 'Cancellation Failed',
        message: getOrderErrorMessage(error),
      });
    },
  });
};
