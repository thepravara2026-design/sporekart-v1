import { useQuery } from '@tanstack/react-query';
import { shippingApi, ShipmentDto, ShipmentTrackingResponseDto } from '../../../services/shippingApi';
import { ORDER_KEYS } from './useOrder';

/** Current shipment record for an order (404 until a shipment exists). */
export const useOrderShipment = (reference: string, enabled = true) => {
  return useQuery({
    queryKey: ORDER_KEYS.shipment(reference),
    queryFn: (): Promise<ShipmentDto> => shippingApi.getCustomerShipment(reference),
    enabled: Boolean(reference) && enabled,
    retry: false,
  });
};

/** Courier tracking timeline for an order's shipment. */
export const useShipmentTracking = (reference: string, enabled = true) => {
  return useQuery({
    queryKey: ORDER_KEYS.tracking(reference),
    queryFn: (): Promise<ShipmentTrackingResponseDto> => shippingApi.getCustomerTracking(reference),
    enabled: Boolean(reference) && enabled,
    retry: false,
  });
};
