package com.sporekart.modules.shipment;

import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
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
class ShippingHandoffTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Test
    @DisplayName("End-to-end fulfilment handoff flow: Order -> Fulfilment -> Shipping Provider -> Delivery -> Order Delivered")
    void testEndToEndFulfilmentHandoff() {
        // 1. Create order
        AddressSnapshot addr = new AddressSnapshot("E2E Customer", "9988776655", "789 Tech Park", "Suite 100", "Hyderabad", "Telangana", "500081", "India");
        Order order = Order.createNewOrder(
                "ORD-E2E-" + UUID.randomUUID().toString().substring(0, 6),
                "cust-e2e-1",
                "INR",
                BigDecimal.valueOf(5000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(5000),
                "IDEM-E2E-" + UUID.randomUUID(),
                addr,
                "Notes",
                List.of()
        );
        orderRepository.save(order);

        // 2. Payment confirmed
        orderApplicationService.confirmOrderPayment(order.getId(), "PAY-E2E-100");

        // 3. Admin starts processing and marks ready for fulfilment
        orderApplicationService.startProcessing(order.getId(), "admin-ops");
        OrderDto readyOrder = orderApplicationService.markReadyForFulfilment(order.getId(), "admin-ops");
        assertThat(readyOrder.status()).isEqualTo(OrderStatus.READY_FOR_FULFILMENT);

        // 4. Shipment auto-creation & booking
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());
        assertThat(shipment.status()).isEqualTo(ShipmentStatus.BOOKED);
        assertThat(shipment.awb()).isNotNull();

        // Check Order transitioned to SHIPPED
        OrderDto shippedOrder = orderApplicationService.getOrderDetail("cust-e2e-1", order.getId());
        assertThat(shippedOrder.status()).isEqualTo(OrderStatus.SHIPPED);

        // 5. Provider Webhook: OUT_FOR_DELIVERY
        String eventOfd = "evt_ofd_" + UUID.randomUUID();
        String ofdBody = String.format("""
                {
                  "event_id": "%s",
                  "provider_shipment_id": "%s",
                  "awb": "%s",
                  "order_reference": "%s",
                  "status": "OUT_FOR_DELIVERY",
                  "description": "Out for delivery with courier agent",
                  "location": "Hyderabad Hub"
                }
                """, eventOfd, shipment.providerShipmentId(), shipment.awb(), shipment.orderReference());

        shipmentService.processWebhook(ShipmentProviderType.MOCK, ofdBody, Map.of());

        OrderDto ofdOrder = orderApplicationService.getOrderDetail("cust-e2e-1", order.getId());
        assertThat(ofdOrder.status()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);

        // 6. Provider Webhook: DELIVERED
        String eventDel = "evt_del_" + UUID.randomUUID();
        String delBody = String.format("""
                {
                  "event_id": "%s",
                  "provider_shipment_id": "%s",
                  "awb": "%s",
                  "order_reference": "%s",
                  "status": "DELIVERED",
                  "description": "Package delivered successfully to recipient",
                  "location": "Hyderabad Destination"
                }
                """, eventDel, shipment.providerShipmentId(), shipment.awb(), shipment.orderReference());

        shipmentService.processWebhook(ShipmentProviderType.MOCK, delBody, Map.of());

        // Verify Shipment status is DELIVERED
        ShipmentDto finalShipment = shipmentService.getAdminShipmentByReference(shipment.shipmentReference());
        assertThat(finalShipment.status()).isEqualTo(ShipmentStatus.DELIVERED);

        // Verify Order status is DELIVERED
        OrderDto finalOrder = orderApplicationService.getOrderDetail("cust-e2e-1", order.getId());
        assertThat(finalOrder.status()).isEqualTo(OrderStatus.DELIVERED);
    }
}
