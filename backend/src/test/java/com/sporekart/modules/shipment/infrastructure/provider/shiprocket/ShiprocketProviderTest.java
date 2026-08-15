package com.sporekart.modules.shipment.infrastructure.provider.shiprocket;

import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;
import com.sporekart.modules.shipment.infrastructure.provider.dto.NormalizedWebhookEvent;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentTrackingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShiprocketProviderTest {

    private ShiprocketShippingProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ShiprocketShippingProvider();
    }

    @Test
    @DisplayName("Should return correct provider type")
    void testProviderType() {
        assertThat(provider.getProviderType()).isEqualTo(ShipmentProviderType.SHIPROCKET);
    }

    @Test
    @DisplayName("Should generate valid cached auth token")
    void testAuthTokenGeneration() {
        String token1 = provider.getAuthToken();
        String token2 = provider.getAuthToken();
        assertThat(token1).isNotBlank();
        assertThat(token1).startsWith("sr_token_");
        assertThat(token1).isEqualTo(token2); // Cached
    }

    @Test
    @DisplayName("Should create and book shipment successfully")
    void testCreateAndBookShipmentSuccess() {
        ShippingAddressSnapshot addr = new ShippingAddressSnapshot(
                "Alice Smith", "9876543210", "Flat 101", "Street 2", "Bangalore", "Karnataka", "560001", "India"
        );
        PackageDetails pkg = new PackageDetails(600, 150, 150, 150, BigDecimal.valueOf(1500));
        ShipmentBookingRequest request = new ShipmentBookingRequest(
                "SHP-SR-1001", "ORD-SR-1001", addr, pkg,
                List.of(new ShipmentBookingRequest.BookingItem("SKU-PROD-1", "Product A", 2))
        );

        ShipmentBookingResult result = provider.createAndBookShipment(request);

        assertThat(result.success()).isTrue();
        assertThat(result.providerShipmentId()).startsWith("SR-SHP-");
        assertThat(result.awb()).startsWith("SR-AWB-");
        assertThat(result.courierName()).contains("Shiprocket");
        assertThat(result.estimatedDeliveryAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle shipment booking failure scenario")
    void testCreateAndBookShipmentFailure() {
        ShippingAddressSnapshot addr = new ShippingAddressSnapshot(
                "Alice Smith", "9876543210", "Flat 101", "Street 2", "Bangalore", "Karnataka", "560001", "India"
        );
        PackageDetails pkg = new PackageDetails(600, 150, 150, 150, BigDecimal.valueOf(1500));
        ShipmentBookingRequest request = new ShipmentBookingRequest(
                "FAIL_SHIPROCKET", "ORD-SR-1001", addr, pkg,
                List.of(new ShipmentBookingRequest.BookingItem("SKU-PROD-1", "Product A", 2))
        );

        ShipmentBookingResult result = provider.createAndBookShipment(request);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Invalid pickup Pincode");
    }

    @Test
    @DisplayName("Should fetch tracking info with checkpoints")
    void testGetTrackingInfo() {
        ShipmentTrackingResult result = provider.getTrackingInfo("SR-SHP-999", "SR-AWB-999");

        assertThat(result.success()).isTrue();
        assertThat(result.checkpoints()).hasSize(2);
        assertThat(result.currentStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Should cancel shipment successfully")
    void testCancelShipment() {
        ShipmentCancellationRequest req = new ShipmentCancellationRequest(
                "SHP-SR-1001", "SR-SHP-999", "SR-AWB-999", "Customer requested cancellation"
        );
        ShipmentCancellationResult res = provider.cancelShipment(req);

        assertThat(res.success()).isTrue();
        assertThat(res.message()).contains("cancelled");
    }

    @Test
    @DisplayName("Should verify webhook token headers correctly")
    void testVerifyWebhookSignature() {
        String body = "{\"event_id\":\"sr-evt-1\"}";

        assertThat(provider.verifyWebhookSignature(body, Map.of("x-api-key", "shiprocket_secret_token_123"))).isTrue();
        assertThat(provider.verifyWebhookSignature(body, Map.of("x-api-key", "INVALID_TOKEN"))).isFalse();
        assertThat(provider.verifyWebhookSignature(body, Map.of("x-shiprocket-token", "INVALID_TOKEN"))).isFalse();
    }

    @Test
    @DisplayName("Should parse webhook payload into NormalizedWebhookEvent")
    void testParseWebhookEvent() {
        String body = """
                {
                  "event_id": "sr-evt-555",
                  "shipment_id": "SR-SHP-555",
                  "awb": "SR-AWB-555",
                  "order_id": "ORD-SR-555",
                  "current_status": "OUT FOR DELIVERY",
                  "courier_custom_status": "Out for delivery with agent",
                  "location": "Bangalore Central Hub"
                }
                """;

        NormalizedWebhookEvent event = provider.parseWebhookEvent(body);

        assertThat(event.providerEventId()).isEqualTo("sr-evt-555");
        assertThat(event.providerShipmentId()).isEqualTo("SR-SHP-555");
        assertThat(event.awb()).isEqualTo("SR-AWB-555");
        assertThat(event.orderReference()).isEqualTo("ORD-SR-555");
        assertThat(event.normalizedStatus()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);
        assertThat(event.location()).isEqualTo("Bangalore Central Hub");
    }
}
