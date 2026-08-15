package com.sporekart.modules.shipment.infrastructure.provider.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.infrastructure.provider.ShippingProvider;
import com.sporekart.modules.shipment.infrastructure.provider.dto.NormalizedWebhookEvent;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentTrackingResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MockShippingProvider implements ShippingProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ShipmentProviderType getProviderType() {
        return ShipmentProviderType.MOCK;
    }

    @Override
    public ShipmentBookingResult createAndBookShipment(ShipmentBookingRequest request) {
        if ("FAIL_BOOKING".equalsIgnoreCase(request.shipmentReference())) {
            return ShipmentBookingResult.failure("Mock shipping provider forced booking failure");
        }
        String providerShipmentId = "MOCK-SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String awb = "MOCK-AWB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String trackingNumber = "TRK-" + awb;
        Instant estDelivery = Instant.now().plusSeconds(86400 * 3);

        return ShipmentBookingResult.success(
                providerShipmentId,
                awb,
                trackingNumber,
                "Mock Express Shipping",
                "MOCK_EXPRESS",
                estDelivery
        );
    }

    @Override
    public ShipmentCancellationResult cancelShipment(ShipmentCancellationRequest request) {
        if ("FAIL_CANCEL".equalsIgnoreCase(request.awb())) {
            return ShipmentCancellationResult.failure("Mock cancellation failed");
        }
        return ShipmentCancellationResult.success("Mock shipment successfully cancelled");
    }

    @Override
    public ShipmentTrackingResult getTrackingInfo(String providerShipmentId, String awb) {
        Instant now = Instant.now();
        List<ShipmentTrackingResult.TrackingCheckpoint> checkpoints = List.of(
                new ShipmentTrackingResult.TrackingCheckpoint(
                        "evt-mock-1",
                        "PICKED_UP",
                        ShipmentStatus.PICKED_UP,
                        "Package picked up at warehouse",
                        "Hub Alpha",
                        now.minusSeconds(7200)
                ),
                new ShipmentTrackingResult.TrackingCheckpoint(
                        "evt-mock-2",
                        "IN_TRANSIT",
                        ShipmentStatus.IN_TRANSIT,
                        "Package in transit to sorting center",
                        "Hub Beta",
                        now.minusSeconds(3600)
                )
        );

        return new ShipmentTrackingResult(
                true,
                providerShipmentId,
                awb,
                ShipmentStatus.IN_TRANSIT,
                checkpoints,
                null
        );
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, Map<String, String> headers) {
        if (headers != null && "INVALID_SIG".equalsIgnoreCase(headers.get("x-mock-signature"))) {
            return false;
        }
        return true;
    }

    @Override
    public NormalizedWebhookEvent parseWebhookEvent(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String eventId = root.path("event_id").asText("evt-" + UUID.randomUUID());
            String shipmentId = root.path("provider_shipment_id").asText("MOCK-SP-UNKNOWN");
            String awb = root.path("awb").asText("MOCK-AWB-UNKNOWN");
            String orderRef = root.path("order_reference").asText();
            String rawStatus = root.path("status").asText("IN_TRANSIT");
            String desc = root.path("description").asText("Mock status update");
            String location = root.path("location").asText("Mock Facility");

            ShipmentStatus status = mapStatus(rawStatus);

            return new NormalizedWebhookEvent(
                    eventId,
                    shipmentId,
                    awb,
                    orderRef,
                    rawStatus,
                    status,
                    desc,
                    location,
                    Instant.now()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse mock webhook event payload: " + e.getMessage(), e);
        }
    }

    private ShipmentStatus mapStatus(String rawStatus) {
        return switch (rawStatus.toUpperCase()) {
            case "PICKED_UP" -> ShipmentStatus.PICKED_UP;
            case "IN_TRANSIT" -> ShipmentStatus.IN_TRANSIT;
            case "OUT_FOR_DELIVERY" -> ShipmentStatus.OUT_FOR_DELIVERY;
            case "DELIVERED" -> ShipmentStatus.DELIVERED;
            case "DELIVERY_FAILED" -> ShipmentStatus.DELIVERY_FAILED;
            case "CANCELLED" -> ShipmentStatus.CANCELLED;
            case "RTO_INITIATED" -> ShipmentStatus.RTO_INITIATED;
            default -> ShipmentStatus.IN_TRANSIT;
        };
    }
}
