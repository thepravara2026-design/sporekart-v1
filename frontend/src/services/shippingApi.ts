import { axiosInstance } from './apiClient';
import { ENDPOINTS } from './endpoints';

export interface TrackingEventDto {
  id?: string;
  providerEventId?: string;
  providerStatus: string;
  normalizedStatus: string;
  description: string;
  location?: string;
  occurredAt: string;
}

export interface ShipmentTrackingResponseDto {
  shipmentReference: string;
  orderReference: string;
  status: string;
  awb?: string;
  courierName?: string;
  estimatedDeliveryAt?: string;
  timeline: TrackingEventDto[];
}

export interface ShipmentItemDto {
  id: string;
  shipmentId: string;
  orderItemId: string;
  productId: string;
  sku: string;
  productNameSnapshot: string;
  quantity: number;
}

export interface ShipmentDto {
  id: string;
  shipmentReference: string;
  orderId: string;
  orderReference: string;
  customerId: string;
  status: string;
  provider: string;
  providerShipmentId?: string;
  awb?: string;
  trackingNumber?: string;
  courierName?: string;
  courierCode?: string;
  packageDetails?: {
    weightKg?: number;
    lengthCm?: number;
    widthCm?: number;
    heightCm?: number;
    declaredValue?: number;
  };
  shippingAddress?: {
    fullName: string;
    phone?: string;
    addressLine1: string;
    addressLine2?: string;
    city: string;
    state: string;
    postalCode: string;
    country?: string;
  };
  items?: ShipmentItemDto[];
  estimatedDeliveryAt?: string;
  bookedAt?: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  statusHistories?: Array<{
    id: string;
    previousStatus?: string;
    newStatus: string;
    reason?: string;
    actorType: string;
    actorId?: string;
    createdAt: string;
  }>;
  trackingEvents: TrackingEventDto[];
  version?: number;
  createdAt: string;
  updatedAt: string;
}

export const shippingApi = {
  getCustomerShipment: async (orderRef: string): Promise<ShipmentDto> => {
    const response = await axiosInstance.get<ShipmentDto>(ENDPOINTS.CUSTOMER_SHIPMENT(orderRef));
    return response.data;
  },

  getCustomerTracking: async (orderRef: string): Promise<ShipmentTrackingResponseDto> => {
    const response = await axiosInstance.get<ShipmentTrackingResponseDto>(ENDPOINTS.CUSTOMER_SHIPMENT_TRACKING(orderRef));
    return response.data;
  },

  listAdminShipments: async (status?: string): Promise<ShipmentDto[]> => {
    const response = await axiosInstance.get<{ content: ShipmentDto[] }>(ENDPOINTS.ADMIN_SHIPMENTS, {
      params: { status }
    });
    return response.data.content;
  },

  getAdminShipmentDetail: async (shipmentRef: string): Promise<ShipmentDto> => {
    const response = await axiosInstance.get<ShipmentDto>(ENDPOINTS.ADMIN_SHIPMENT_BY_REF(shipmentRef));
    return response.data;
  },

  retryBooking: async (shipmentRef: string): Promise<ShipmentDto> => {
    const response = await axiosInstance.post<ShipmentDto>(ENDPOINTS.ADMIN_SHIPMENT_RETRY(shipmentRef));
    return response.data;
  },

  syncShipment: async (shipmentRef: string): Promise<ShipmentDto> => {
    const response = await axiosInstance.post<ShipmentDto>(ENDPOINTS.ADMIN_SHIPMENT_SYNC(shipmentRef));
    return response.data;
  },

  cancelShipment: async (shipmentRef: string, reason?: string): Promise<ShipmentDto> => {
    const response = await axiosInstance.post<ShipmentDto>(
      ENDPOINTS.ADMIN_SHIPMENT_CANCEL(shipmentRef),
      { reason: reason ?? 'Admin cancellation' }
    );
    return response.data;
  },

  getLabelUrl: async (shipmentRef: string): Promise<string> => {
    const response = await axiosInstance.get<{ labelUrl: string }>(ENDPOINTS.ADMIN_SHIPMENT_LABEL(shipmentRef));
    return response.data.labelUrl;
  },

  getManifestUrl: async (shipmentRef: string): Promise<string> => {
    const response = await axiosInstance.get<{ manifestUrl: string }>(ENDPOINTS.ADMIN_SHIPMENT_MANIFEST(shipmentRef));
    return response.data.manifestUrl;
  }
};
