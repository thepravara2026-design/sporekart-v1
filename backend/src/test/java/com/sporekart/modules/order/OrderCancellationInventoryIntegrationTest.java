package com.sporekart.modules.order;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.*;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderCancellationInventoryIntegrationTest {

    @Autowired
    private InventoryApplicationService inventoryApplicationService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("ORD-INV-001: Cancelling an order explicitly releases stock reservation and restores available inventory")
    void testOrderCancellationReleasesStockReservation() {
        String sku = "SKU-CANC-TEST-01";
        UUID productId = UUID.randomUUID();
        InventoryItem item = InventoryItem.createNew(productId, null, sku, 50);
        inventoryRepository.save(item);

        UUID orderId = UUID.randomUUID();
        String customerId = "cust-canc-01";
        UUID itemId = UUID.randomUUID();

        OrderItem orderItem = new OrderItem(
                itemId,
                orderId,
                productId,
                null,
                sku,
                "Oyster Spawn",
                "Default",
                new BigDecimal("20.00"),
                5,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                OffsetDateTime.now()
        );

        AddressSnapshot address = new AddressSnapshot(
                "John Doe",
                "+15550199",
                "123 Main St",
                null,
                "City",
                "State",
                "10001",
                "IN"
        );

        Order order = new Order(
                orderId,
                "ORD-CANC-001",
                customerId,
                OrderStatus.CREATED,
                "INR",
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                "idem-key-1",
                address,
                "Cancel test order",
                "grower-1",
                List.of(orderItem),
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        orderRepository.save(order);

        // Reserve inventory for order
        ReservationDto reservation = inventoryApplicationService.reserveInventoryForOrder(orderId, customerId);
        assertThat(reservation.status()).isEqualTo(ReservationStatus.ACTIVE);

        InventoryItem reservedItem = inventoryRepository.findBySku(sku).orElseThrow();
        assertThat(reservedItem.getReservedQuantity()).isEqualTo(5);
        assertThat(restoredAvailableQuantity(reservedItem)).isEqualTo(45);

        // Cancel order
        OrderDto cancelledOrder = orderApplicationService.cancelOrder(customerId, orderId, "Customer requested cancellation");

        assertThat(cancelledOrder.status()).isEqualTo(OrderStatus.CANCELLED);

        // Verify reservation is now RELEASED
        var reservationEntity = reservationRepository.findByOrderId(orderId).orElseThrow();
        assertThat(reservationEntity.getStatus()).isEqualTo(ReservationStatus.RELEASED);

        // Verify inventory available stock is restored
        InventoryItem restoredItem = inventoryRepository.findBySku(sku).orElseThrow();
        assertThat(restoredItem.getReservedQuantity()).isEqualTo(0);
        assertThat(restoredItem.getOnHandQuantity()).isEqualTo(50);
    }

    private int restoredAvailableQuantity(InventoryItem item) {
        return item.getOnHandQuantity() - item.getReservedQuantity();
    }
}
