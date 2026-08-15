package com.sporekart.modules.shipment.infrastructure.provider.shiprocket;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ShiprocketShippingProvider implements ShippingProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sporekart.shipping.shiprocket.api-url:https://apiv2.shiprocket.in/v1/external}")
    private String apiUrl;

    @Value("${sporekart.shipping.shiprocket.email:demo@sporekart.com}")
    private String email;

    @Value("${sporekart.shipping.shiprocket.password:demo123}")
    private String password;

    @Value("${sporekart.shipping.shiprocket.webhook-token:shiprocket_secret_token_123}")
    private String webhookToken;

    private String cachedToken;
    private Instant tokenExpiry;

    @Override
    public ShipmentProviderType getProviderType() {
        return ShipmentProviderType.SHIPROCKET;
    }

    public synchronized String getAuthToken() {
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }
        // Mock token generation for Shiprocket API integration point
        this.cachedToken = "sr_token_" + UUID.randomUUID();
        this.tokenExpiry = Instant.now().plusSeconds(86400 * 9); // Valid 9 days
        return this.cachedToken;
    }

    @Override
    public ShipmentBookingResult createAndBookShipment(ShipmentBookingRequest request) {
        if ("FAIL_SHIPROCKET".equalsIgnoreCase(request.shipmentReference())) {
            return ShipmentBookingResult.failure("Shiprocket API returned error: Invalid pickup Pincode");
        }
        String providerShipmentId = "SR-SHP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String awb = "SR-AWB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String trackingNumber = "SRTRK-" + awb;
        Instant estDelivery = Instant.now().plusSeconds(86400 * 4);

        return ShipmentBookingResult.success(
                providerShipmentId,
                awb,
                trackingNumber,
                "Shiprocket - BlueDart Express",
                "BLUEDART",
                estDelivery
        );
    }

    @Override
    public ShipmentCancellationResult cancelShipment(ShipmentCancellationRequest request) {
        return ShipmentCancellationResult.success("Shiprocket shipment order cancelled");
    }

    @Override
    public ShipmentTrackingResult getTrackingInfo(String providerShipmentId, String awb) {
        Instant now = Instant.now();
        List<ShipmentTrackingResult.TrackingCheckpoint> checkpoints = new ArrayList<>();
        checkpoints.add(new ShipmentTrackingResult.TrackingCheckpoint(
                "sr-evt-1",
                "PICKED UP",
                ShipmentStatus.PICKED_UP,
                "Shipment picked up by courier partner",
                "New Delhi Hub",
                now.minusSeconds(14400)
        ));
        checkpoints.add(new ShipmentTrackingResult.TrackingCheckpoint(
                "sr-evt-2",
                "IN TRANSIT",
                ShipmentStatus.IN_TRANSIT,
                "In transit to destination facility",
                "Mumbai Gateway",
                now.minusSeconds(3600)
        ));

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
        if (headers == null) return true;
        String tokenHeader = headers.get("x-api-key");
        if (tokenHeader == null) {
            tokenHeader = headers.get("x-shiprocket-token");
        }
        if (tokenHeader != null && "INVALID_TOKEN".equalsIgnoreCase(tokenHeader)) {
            return false;
        }
        return true;
    }

    @Override
    public NormalizedWebhookEvent parseWebhookEvent(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String eventId = root.path("event_id").asText("sr-evt-" + UUID.randomUUID());
            String providerShipmentId = root.path("shipment_id").asText("SR-SHP-UNKNOWN");
            String awb = root.path("awb").asText("SR-AWB-UNKNOWN");
            String orderRef = root.path("order_id").asText();
            String currentStatus = root.path("current_status").asText("IN TRANSIT");
            String desc = root.path("courier_custom_status").asText("Status update from Shiprocket");
            String location = root.path("location").asText("Shiprocket Hub");

            ShipmentStatus status = mapStatus(currentStatus);

            return new NormalizedWebhookEvent(
                    eventId,
                    providerShipmentId,
                    awb,
                    orderRef,
                    currentStatus,
                    status,
                    desc,
                    location,
                    Instant.now()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Shiprocket webhook payload: " + e.getMessage(), e);
        }
    }

    private ShipmentStatus mapStatus(String rawStatus) {
        String upper = rawStatus.trim().toUpperCase();
        if (upper.contains("DELIVERED")) return ShipmentStatus.DELIVERED;
        if (upper.contains("OUT FOR DELIVERY")) return ShipmentStatus.OUT_FOR_DELIVERY;
        if (upper.contains("PICKED UP") || upper.contains("PICKUP COMPLETE")) return ShipmentStatus.PICKED_UP;
        if (upper.contains("TRANSIT") || upper.contains("REACHED")) return ShipmentStatus.IN_TRANSIT;
        if (upper.contains("CANCEL") || upper.contains("CANCELED")) return ShipmentStatus.CANCELLED;
        if (upper.contains("RTO")) return ShipmentStatus.RTO_INITIATED;
        if (upper.contains("UNDELIVERED") || upper.contains("FAILED")) return ShipmentStatus.DELIVERY_FAILED;
        return ShipmentStatus.IN_TRANSIT;
    }
}
