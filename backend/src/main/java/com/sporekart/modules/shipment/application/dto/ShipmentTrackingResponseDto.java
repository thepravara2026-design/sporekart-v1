package com.sporekart.modules.shipment.application.dto;

import com.sporekart.modules.shipment.domain.ShipmentStatus;

import java.time.Instant;
import java.util.List;

public record ShipmentTrackingResponseDto(
        String shipmentReference,
        String orderReference,
        ShipmentStatus status,
        String awb,
        String courierName,
        Instant estimatedDeliveryAt,
        List<ShipmentTrackingEventDto> timeline
) {}
