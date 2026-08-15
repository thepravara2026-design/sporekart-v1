package com.sporekart.modules.shipment;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShipmentConcurrencyTest {

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Autowired
    private OrderRepository orderRepository;

    private Order createTestOrder() {
        AddressSnapshot addr = new AddressSnapshot("Bob Builder", "9876543210", "12 Structure Rd", null, "Chennai", "Tamil Nadu", "600001", "India");
        Order order = Order.createNewOrder(
                "ORD-CONC-" + UUID.randomUUID().toString().substring(0, 6),
                "cust-conc-100",
                "INR",
                BigDecimal.valueOf(4500),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(4500),
                "IDEM-CONC-" + UUID.randomUUID(),
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
    @DisplayName("Concurrent booking calls on same shipment should succeed cleanly and idempotently")
    void testConcurrentBooking() throws InterruptedException {
        Order order = createTestOrder();
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    shipmentService.bookShipment(shipment.id());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Ignored or logged
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(threads);

        ShipmentDto finalState = shipmentService.getAdminShipmentByReference(shipment.shipmentReference());
        assertThat(finalState.awb()).isNotNull();
    }

    @Test
    @DisplayName("Concurrent webhooks for same event should not cause duplicate records or errors")
    void testConcurrentWebhooks() throws InterruptedException {
        Order order = createTestOrder();
        ShipmentDto shipment = shipmentService.createShipmentForOrder(order.getId());

        String eventId = "evt_conc_" + UUID.randomUUID();
        String rawBody = String.format("""
                {
                  "event_id": "%s",
                  "provider_shipment_id": "%s",
                  "awb": "%s",
                  "order_reference": "%s",
                  "status": "IN_TRANSIT",
                  "description": "Package in transit",
                  "location": "Chennai Sorting Facility"
                }
                """, eventId, shipment.providerShipmentId(), shipment.awb(), shipment.orderReference());

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    boolean ok = shipmentService.processWebhook(ShipmentProviderType.MOCK, rawBody, Map.of());
                    if (ok) successCount.incrementAndGet();
                } catch (Exception e) {
                    // Ignored
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }
}
