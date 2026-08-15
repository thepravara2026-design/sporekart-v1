package com.sporekart.modules.shipment.application.dto;

import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ShipmentDto(
        UUID id,
        String shipmentReference,
        UUID orderId,
        String orderReference,
        String customerId,
        ShipmentStatus status,
        ShipmentProviderType provider,
        String providerShipmentId,
        String awb,
        String trackingNumber,
        String courierName,
        String courierCode,
        PackageDetails packageDetails,
        ShippingAddressSnapshot shippingAddress,
        List<ShipmentItemDto> items,
        Instant estimatedDeliveryAt,
        Instant bookedAt,
        Instant pickedUpAt,
        Instant deliveredAt,
        List<ShipmentStatusHistoryDto> statusHistories,
        List<ShipmentTrackingEventDto> trackingEvents,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ShipmentDto fromDomain(Shipment shipment) {
        return new ShipmentDto(
                shipment.getId(),
                shipment.getShipmentReference(),
                shipment.getOrderId(),
                shipment.getOrderReference(),
                shipment.getCustomerId(),
                shipment.getStatus(),
                shipment.getProvider(),
                shipment.getProviderShipmentId(),
                shipment.getAwb(),
                shipment.getTrackingNumber(),
                shipment.getCourierName(),
                shipment.getCourierCode(),
                shipment.getPackageDetails(),
                shipment.getShippingAddress(),
                shipment.getItems().stream().map(ShipmentItemDto::fromDomain).collect(Collectors.toList()),
                shipment.getEstimatedDeliveryAt(),
                shipment.getBookedAt(),
                shipment.getPickedUpAt(),
                shipment.getDeliveredAt(),
                shipment.getStatusHistories().stream().map(ShipmentStatusHistoryDto::fromDomain).collect(Collectors.toList()),
                shipment.getTrackingEvents().stream().map(ShipmentTrackingEventDto::fromDomain).collect(Collectors.toList()),
                shipment.getVersion(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}
