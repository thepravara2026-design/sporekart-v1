package com.sporekart.modules.shipment.infrastructure.provider.dto;

public record ShipmentCancellationRequest(
        String shipmentReference,
        String providerShipmentId,
        String awb,
        String reason
) {}
