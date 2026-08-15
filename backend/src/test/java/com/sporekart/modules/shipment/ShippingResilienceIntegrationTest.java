package com.sporekart.modules.shipment;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.shipment.application.ShipmentReconciliationService;
import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ShippingResilienceIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentReconciliationService reconciliationService;

    @Test
    @Transactional
    @DisplayName("Should protect against out-of-order tracking events and prevent state regression")
    void shouldPreventStateRegressionOnOutOfOrderEvents() {
        Shipment shipment = createTestShipment();
        shipment.markReadyForBooking(OrderActorType.SYSTEM, "SYSTEM");
        shipment.markBookingPending(OrderActorType.SYSTEM, "SYSTEM");
        shipment.markBooked("PROV-SHP-101", "AWB-101", "TRACK-101", "Express Courier", "EXPRESS", null, OrderActorType.SYSTEM, "SYSTEM");
        shipment.markDelivered(null, OrderActorType.SYSTEM, "SYSTEM", "EVT-DELIVERED-001");
        shipmentRepository.save(shipment);

        // Simulate late arrival of out-of-order SHIPPED / IN_TRANSIT event
        shipment.addTrackingEvent("EVT-LATE-002", "IN_TRANSIT", ShipmentStatus.IN_TRANSIT, "Late tracking update received", "Hub B", null);
        shipmentRepository.save(shipment);

        Shipment reloaded = shipmentRepository.findById(shipment.getId()).orElseThrow();
        // State remains DELIVERED (terminal, no regression)
        assertThat(reloaded.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        // Event was appended safely for audit
        assertThat(reloaded.getTrackingEvents()).hasSize(1);
    }

    @Test
    @Transactional
    @DisplayName("ShipmentReconciliationService should reconcile BOOKING_PENDING shipments")
    void shouldReconcileBookingPendingShipments() {
        Shipment shipment = createTestShipment();
        shipment.markReadyForBooking(OrderActorType.SYSTEM, "SYSTEM");
        shipment.markBookingPending(OrderActorType.SYSTEM, "SYSTEM");
        try {
            var awbField = Shipment.class.getDeclaredField("awb");
            awbField.setAccessible(true);
            awbField.set(shipment, "AWB-REC-999");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        shipmentRepository.save(shipment);

        reconciliationService.reconcilePendingShipments();

        Shipment reconciled = shipmentRepository.findById(shipment.getId()).orElseThrow();
        assertThat(reconciled.getStatus()).isEqualTo(ShipmentStatus.BOOKED);
    }

    private Shipment createTestShipment() {
        PackageDetails pkg = new PackageDetails(1500, 200, 150, 100, new BigDecimal("500.00"));
        ShippingAddressSnapshot addr = new ShippingAddressSnapshot("John Doe", "9999999999", "123 Main St", "Suite 4", "Bangalore", "Karnataka", "560001", "IN");
        return Shipment.create("SHP-RES-" + UUID.randomUUID().toString().substring(0, 8), UUID.randomUUID(), "ORD-RES-101", "CUST-101", ShipmentProviderType.MOCK, pkg, addr, List.of());
    }
}
