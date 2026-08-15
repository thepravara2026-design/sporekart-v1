package com.sporekart.modules.shipment;

import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;
import com.sporekart.modules.shipment.infrastructure.provider.ShippingProvider;
import com.sporekart.modules.shipment.infrastructure.provider.dto.NormalizedWebhookEvent;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentTrackingResult;
import com.sporekart.modules.shipment.infrastructure.provider.mock.MockShippingProvider;
import com.sporekart.modules.shipment.infrastructure.provider.shiprocket.ShiprocketShippingProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingProviderContractTest {

    private final MockShippingProvider mockProvider = new MockShippingProvider();
    private final ShiprocketShippingProvider shiprocketProvider = new ShiprocketShippingProvider();

    @Test
    @DisplayName("Mock provider should satisfy booking and tracking contract")
    void testMockProviderContract() {
        verifyProviderContract(mockProvider);
    }

    @Test
    @DisplayName("Shiprocket provider should satisfy booking and tracking contract")
    void testShiprocketProviderContract() {
        verifyProviderContract(shiprocketProvider);
    }

    private void verifyProviderContract(ShippingProvider provider) {
        ShippingAddressSnapshot addr = new ShippingAddressSnapshot(
                "John Customer", "9998887770", "Line 1", "Line 2", "Delhi", "Delhi", "110001", "India"
        );
        PackageDetails pkg = new PackageDetails(500, 100, 100, 100, BigDecimal.valueOf(2000));
        ShipmentBookingRequest req = new ShipmentBookingRequest(
                "SHP-CONTRACT-1",
                "ORD-CONTRACT-1",
                addr,
                pkg,
                List.of(new ShipmentBookingRequest.BookingItem("SKU-1", "Prod 1", 1))
        );

        ShipmentBookingResult bookingResult = provider.createAndBookShipment(req);
        assertThat(bookingResult.success()).isTrue();
        assertThat(bookingResult.awb()).isNotBlank();
        assertThat(bookingResult.providerShipmentId()).isNotBlank();

        ShipmentTrackingResult trackingResult = provider.getTrackingInfo(bookingResult.providerShipmentId(), bookingResult.awb());
        assertThat(trackingResult.success()).isTrue();
        assertThat(trackingResult.checkpoints()).isNotEmpty();

        ShipmentCancellationResult cancelResult = provider.cancelShipment(new ShipmentCancellationRequest(
                "SHP-CONTRACT-1", bookingResult.providerShipmentId(), bookingResult.awb(), "Test cancellation"
        ));
        assertThat(cancelResult.success()).isTrue();
    }

    @Test
    @DisplayName("Mock provider webhook parsing and signature check")
    void testMockWebhookParsing() {
        String body = """
                {
                  "event_id": "evt_mock_100",
                  "provider_shipment_id": "MOCK-SP-100",
                  "awb": "MOCK-AWB-100",
                  "order_reference": "ORD-100",
                  "status": "DELIVERED",
                  "description": "Package delivered to customer",
                  "location": "Mumbai Hub"
                }
                """;

        assertThat(mockProvider.verifyWebhookSignature(body, Map.of())).isTrue();
        assertThat(mockProvider.verifyWebhookSignature(body, Map.of("x-mock-signature", "INVALID_SIG"))).isFalse();

        NormalizedWebhookEvent event = mockProvider.parseWebhookEvent(body);
        assertThat(event.providerEventId()).isEqualTo("evt_mock_100");
        assertThat(event.normalizedStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(event.awb()).isEqualTo("MOCK-AWB-100");
    }

    @Test
    @DisplayName("Shiprocket webhook parsing and signature check")
    void testShiprocketWebhookParsing() {
        String body = """
                {
                  "event_id": "sr_evt_200",
                  "shipment_id": "SR-SHP-200",
                  "awb": "SR-AWB-200",
                  "order_id": "ORD-200",
                  "current_status": "DELIVERED",
                  "courier_custom_status": "Delivered successfully",
                  "location": "Delhi Delivery Center"
                }
                """;

        assertThat(shiprocketProvider.verifyWebhookSignature(body, Map.of())).isTrue();
        assertThat(shiprocketProvider.verifyWebhookSignature(body, Map.of("x-api-key", "INVALID_TOKEN"))).isFalse();

        NormalizedWebhookEvent event = shiprocketProvider.parseWebhookEvent(body);
        assertThat(event.providerEventId()).isEqualTo("sr_evt_200");
        assertThat(event.normalizedStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(event.awb()).isEqualTo("SR-AWB-200");
    }
}
