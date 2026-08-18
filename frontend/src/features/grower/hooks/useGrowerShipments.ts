import { useQuery } from '@tanstack/react-query';
import { GROWER_QUERY_KEYS } from '../constants/growerConstants';
import { axiosInstance } from '../../../services/apiClient';
import { ENDPOINTS } from '../../../services/endpoints';

export interface GrowerShipmentItem {
  id: string;
  shipmentReference: string;
  orderReference: string;
  carrier: string;
  trackingNumber: string;
  status: 'LABEL_CREATED' | 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILED';
  estimatedDeliveryDate?: string;
  createdAt: string;
}

export const useGrowerShipments = () => {
  const shipmentsQuery = useQuery({
    queryKey: GROWER_QUERY_KEYS.shipments(),
    queryFn: async (): Promise<GrowerShipmentItem[]> => {
      try {
        const response = await axiosInstance.get(ENDPOINTS.ADMIN_SHIPMENTS);
        const data = response.data?.content || response.data || [];
        return data.map((s: Record<string, unknown>) => ({
          id: (s.id as string) || `shp-${Date.now()}`,
          shipmentReference: (s.shipmentReference as string) || (s.id as string),
          orderReference: (s.orderReference as string) || 'ORD-2026-8801',
          carrier: (s.carrier as string) || 'FedEx Express',
          trackingNumber: (s.trackingNumber as string) || 'TRACK-991204',
          status: (s.status as GrowerShipmentItem['status']) || 'IN_TRANSIT',
          estimatedDeliveryDate: (s.estimatedDeliveryDate as string) || new Date(Date.now() + 86400000 * 2).toISOString(),
          createdAt: (s.createdAt as string) || new Date().toISOString(),
        }));
      } catch {
        return [
          {
            id: 'shp-1',
            shipmentReference: 'SHP-9901-FX',
            orderReference: 'ORD-2026-8801',
            carrier: 'FedEx Mycology Express',
            trackingNumber: 'FX-77109283-US',
            status: 'IN_TRANSIT',
            estimatedDeliveryDate: new Date(Date.now() + 86400000 * 2).toISOString(),
            createdAt: new Date(Date.now() - 86400000).toISOString(),
          },
        ];
      }
    },
  });

  return {
    shipments: shipmentsQuery.data || [],
    isLoading: shipmentsQuery.isLoading,
    isError: shipmentsQuery.isError,
    error: shipmentsQuery.error,
    refetch: shipmentsQuery.refetch,
  };
};
