package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockMovement;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.domain.StockReservationItem;
import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;
import com.sporekart.modules.inventory.infrastructure.config.InventoryProperties;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.inventory.infrastructure.persistence.StockMovementRepository;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;

import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryApplicationServiceTest {

    private InventoryRepository inventoryRepository;
    private ReservationRepository reservationRepository;
    private StockMovementRepository stockMovementRepository;
    private OrderRepository orderRepository;
    private InventoryProperties inventoryProperties;
    private InventoryApplicationService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        stockMovementRepository = mock(StockMovementRepository.class);
        orderRepository = mock(OrderRepository.class);
        inventoryProperties = new InventoryProperties();
        inventoryService = new InventoryApplicationService(
                inventoryRepository, reservationRepository, stockMovementRepository, orderRepository, inventoryProperties
        );
    }

    @Test
    @DisplayName("Should reserve inventory for order successfully")
    void testReserveInventorySuccess() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-1";
        UUID productId = UUID.randomUUID();
        String sku = "SKU-MUSH-01";

        OrderItem orderItem = new OrderItem(
                UUID.randomUUID(), orderId, productId, null, sku, "Mushroom Spawn", null,
                new BigDecimal("500.00"), 3, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1500.00"),
                new BigDecimal("1500.00"), OffsetDateTime.now()
        );

        AddressSnapshot address = new AddressSnapshot("Jane", "999", "Line", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-001", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("1500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1500.00"), null, address, null, List.of(orderItem), OffsetDateTime.now(), OffsetDateTime.now()
        );

        InventoryItem invItem = InventoryItem.createNew(productId, null, sku, 10);

        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(List.of(sku))).thenReturn(List.of(invItem));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationDto reservation = inventoryService.reserveInventoryForOrder(orderId, customerId);

        assertNotNull(reservation);
        assertEquals(orderId, reservation.orderId());
        assertEquals(ReservationStatus.ACTIVE, reservation.status());
        assertEquals(3, invItem.getReservedQuantity());
        assertEquals(7, invItem.getAvailableQuantity());

        verify(inventoryRepository).save(invItem);
        verify(stockMovementRepository).save(any(StockMovement.class));
        verify(reservationRepository).save(any(StockReservation.class));
    }

    @Test
    @DisplayName("Should replay active reservation when order reservation is called twice")
    void testReserveInventoryIdempotencyReplay() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-1";

        StockReservation existing = StockReservation.createActiveReservation(
                "RES-SPK-001", orderId, OffsetDateTime.now().plusMinutes(15), List.of()
        );

        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        ReservationDto result = inventoryService.reserveInventoryForOrder(orderId, customerId);

        assertNotNull(result);
        assertEquals(existing.getId(), result.id());
        verify(orderRepository, never()).findByIdAndCustomerId(any(), anyString());
        verify(inventoryRepository, never()).findAllBySkuInOrderBySkuAscForUpdate(anyList());
    }

    @Test
    @DisplayName("Should fail reservation when available stock is insufficient")
    void testReserveInventoryInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-1";
        UUID productId = UUID.randomUUID();
        String sku = "SKU-MUSH-02";

        OrderItem orderItem = new OrderItem(
                UUID.randomUUID(), orderId, productId, null, sku, "Mushroom Spawn", null,
                new BigDecimal("500.00"), 15, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("7500.00"),
                new BigDecimal("7500.00"), OffsetDateTime.now()
        );

        AddressSnapshot address = new AddressSnapshot("Jane", "999", "Line", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-002", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("7500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("7500.00"), null, address, null, List.of(orderItem), OffsetDateTime.now(), OffsetDateTime.now()
        );

        InventoryItem invItem = InventoryItem.createNew(productId, null, sku, 10);

        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(List.of(sku))).thenReturn(List.of(invItem));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserveInventoryForOrder(orderId, customerId));
        assertEquals(0, invItem.getReservedQuantity());
    }

    @Test
    @DisplayName("Should release active reservation successfully and restore available stock")
    void testReleaseReservation() {
        UUID reservationId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID inventoryItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String sku = "SKU-RELEASE-1";

        InventoryItem invItem = new InventoryItem(
                inventoryItemId, productId, null, sku, 10, 4, "ACTIVE", 0L, OffsetDateTime.now(), OffsetDateTime.now()
        );

        StockReservationItem resItem = new StockReservationItem(
                UUID.randomUUID(), reservationId, inventoryItemId, productId, null, sku, 4, OffsetDateTime.now()
        );

        StockReservation reservation = new StockReservation(
                reservationId, "RES-001", orderId, ReservationStatus.ACTIVE, OffsetDateTime.now().plusMinutes(10),
                null, List.of(resItem), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(List.of(sku))).thenReturn(List.of(invItem));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationDto released = inventoryService.releaseReservation(reservationId, "CUSTOMER_CANCELLED");

        assertNotNull(released);
        assertEquals(ReservationStatus.RELEASED, released.status());
        assertEquals(0, invItem.getReservedQuantity());
        assertEquals(10, invItem.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should adjust stock on hand via admin operation and record audit log")
    void testAdjustStock() {
        String sku = "SKU-ADJUST-1";
        UUID productId = UUID.randomUUID();
        InventoryItem invItem = InventoryItem.createNew(productId, null, sku, 50);

        when(inventoryRepository.findBySkuForUpdate(sku)).thenReturn(Optional.of(invItem));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StockAdjustmentCommand command = new StockAdjustmentCommand(sku, 120, "NEW_SHIPMENT_RECEIVED");
        InventoryItemDto result = inventoryService.adjustStock(command);

        assertNotNull(result);
        assertEquals(120, result.onHandQuantity());
        verify(stockMovementRepository).save(any(StockMovement.class));
    }
}
