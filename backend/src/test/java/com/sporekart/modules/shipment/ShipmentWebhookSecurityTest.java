package com.sporekart.modules.shipment;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShipmentWebhookSecurityTest {

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order createTestOrder() {
        AddressSnapshot addr = new AddressSnapshot("Alice Smith", "9876543210", "456 Park Ave", null, "Bengaluru", "Karnataka", "560001", "India");
        Order order = Order.createNewOrder(
                "ORD-WH-" + UUID.randomUUID().toString().substring(0, 6),
                "cust-wh-100",
                "INR",
                BigDecimal.valueOf(3000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(3000),
                "IDEM-WH-" + UUID.randomUUID(),
                addr,
                "Notes",
                List.of()
        );
        order.markPaid();
        order.startProcessing();
        order.markReadyForFulfilment();
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("Should reject webhook with invalid signature")
    void testInvalidSignature() {
        Order order = createTestOrder();
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        String rawBody = String.format("""
                {
                  "event_id": "evt_invalid_sig",
                  "provider_shipment_id": "%s",
                  "awb": "%s",
                  "order_reference": "%s",
                  "status": "DELIVERED",
                  "description": "Package delivered",
                  "location": "Bengaluru Hub"
                }
                """, shipment.providerShipmentId(), shipment.awb(), shipment.orderReference());

        boolean success = shipmentService.processWebhook(ShipmentProviderType.MOCK, rawBody, Map.of("x-mock-signature", "INVALID_SIG"));
        assertThat(success).isFalse();
    }

    @Test
    @DisplayName("Should process valid webhook idempotently")
    void testWebhookIdempotency() {
        Order order = createTestOrder();
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        String eventId = "evt_idem_" + UUID.randomUUID();
        String rawBody = String.format("""
                {
                  "event_id": "%s",
                  "provider_shipment_id": "%s",
                  "awb": "%s",
                  "order_reference": "%s",
                  "status": "DELIVERED",
                  "description": "Package delivered to recipient",
                  "location": "Bengaluru Hub"
                }
                """, eventId, shipment.providerShipmentId(), shipment.awb(), shipment.orderReference());

        // First attempt
        boolean firstCall = shipmentService.processWebhook(ShipmentProviderType.MOCK, rawBody, Map.of());
        assertThat(firstCall).isTrue();

        ShipmentDto updated = shipmentService.getAdminShipmentByReference(shipment.shipmentReference());
        assertThat(updated.status()).isEqualTo(ShipmentStatus.DELIVERED);

        // Second identical call (duplicate) should acknowledge cleanly without duplication
        boolean secondCall = shipmentService.processWebhook(ShipmentProviderType.MOCK, rawBody, Map.of());
        assertThat(secondCall).isTrue();
    }
}
