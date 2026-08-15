package com.sporekart.modules.shipment;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentItem;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentDomainTest {

    private Shipment createTestShipment() {
        ShippingAddressSnapshot addr = new ShippingAddressSnapshot(
                "Jane Doe", "9876543210", "123 Main St", "Apt 4B",
                "Mumbai", "Maharashtra", "400001", "India"
        );
        PackageDetails pkg = new PackageDetails(500, 100, 100, 100, BigDecimal.valueOf(1500));
        ShipmentItem item = new ShipmentItem(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "SKU-001", "Test Product", 2);

        return Shipment.create(
                "SHP-TEST-001",
                UUID.randomUUID(),
                "ORD-TEST-001",
                "cust-1001",
                ShipmentProviderType.MOCK,
                pkg,
                addr,
                List.of(item)
        );
    }

    @Test
    @DisplayName("Should create shipment in CREATED state with initial history")
    void testInitialState() {
        Shipment shipment = createTestShipment();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(shipment.getShipmentReference()).isEqualTo("SHP-TEST-001");
        assertThat(shipment.getStatusHistories()).hasSize(1);
        assertThat(shipment.getStatusHistories().get(0).getNewStatus()).isEqualTo(ShipmentStatus.CREATED);
    }

    @Test
    @DisplayName("Should correctly record booking details")
    void testMarkBooked() {
        Shipment shipment = createTestShipment();
        shipment.markReadyForBooking(OrderActorType.SYSTEM, "SYS");
        shipment.markBookingPending(OrderActorType.SYSTEM, "SYS");
        shipment.markBooked(
                "MOCK-SP-123",
                "MOCK-AWB-123",
                "TRK-123",
                "Mock Express",
                "MOCK_EXPRESS",
                Instant.now().plusSeconds(86400 * 2),
                OrderActorType.SYSTEM,
                "SYS"
        );

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.BOOKED);
        assertThat(shipment.getAwb()).isEqualTo("MOCK-AWB-123");
        assertThat(shipment.getProviderShipmentId()).isEqualTo("MOCK-SP-123");
        assertThat(shipment.getStatusHistories()).hasSize(4);
    }

    @Test
    @DisplayName("Should add tracking events and prevent duplicates")
    void testTrackingEvents() {
        Shipment shipment = createTestShipment();
        shipment.markReadyForBooking(OrderActorType.SYSTEM, "SYS");
        shipment.markBookingPending(OrderActorType.SYSTEM, "SYS");
        shipment.markBooked("SP-1", "AWB-1", "TRK-1", "Mock", "MOCK", Instant.now(), OrderActorType.SYSTEM, "SYS");

        shipment.addTrackingEvent("evt-1", "PICKED_UP", ShipmentStatus.PICKED_UP, "Package picked up", "Hub 1", Instant.now());
        assertThat(shipment.getTrackingEvents()).hasSize(1);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PICKED_UP);

        // Duplicate event should be ignored
        shipment.addTrackingEvent("evt-1", "PICKED_UP", ShipmentStatus.PICKED_UP, "Package picked up", "Hub 1", Instant.now());
        assertThat(shipment.getTrackingEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should ignore out-of-order state regression from tracking event")
    void testIgnoreStateRegression() {
        Shipment shipment = createTestShipment();
        shipment.markReadyForBooking(OrderActorType.SYSTEM, "SYS");
        shipment.markBookingPending(OrderActorType.SYSTEM, "SYS");
        shipment.markBooked("SP-1", "AWB-1", "TRK-1", "Mock", "MOCK", Instant.now(), OrderActorType.SYSTEM, "SYS");

        shipment.markPickedUp(Instant.now(), OrderActorType.SYSTEM, "SYS", "evt-1");
        shipment.markInTransit(OrderActorType.SYSTEM, "SYS", "evt-2");
        shipment.markDelivered(Instant.now(), OrderActorType.SYSTEM, "SYS", "evt-3");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);

        // Out-of-order tracking event for IN_TRANSIT arriving after DELIVERED should not regress state
        shipment.addTrackingEvent("evt-late", "IN_TRANSIT", ShipmentStatus.IN_TRANSIT, "Late transit update", "Hub 2", Instant.now());
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    }
}
