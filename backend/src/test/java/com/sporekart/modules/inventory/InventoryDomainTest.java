package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockReservation;

import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;
import com.sporekart.modules.inventory.domain.exception.ReservationAlreadyReleasedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryDomainTest {

    @Test
    @DisplayName("InventoryItem should correctly calculate available quantity and enforce invariants")
    void testInventoryItemInvariants() {
        UUID productId = UUID.randomUUID();
        InventoryItem item = InventoryItem.createNew(productId, null, "SKU-001", 10);

        assertEquals(10, item.getOnHandQuantity());
        assertEquals(0, item.getReservedQuantity());
        assertEquals(10, item.getAvailableQuantity());

        item.reserve(4);
        assertEquals(10, item.getOnHandQuantity());
        assertEquals(4, item.getReservedQuantity());
        assertEquals(6, item.getAvailableQuantity());

        item.release(2);
        assertEquals(10, item.getOnHandQuantity());
        assertEquals(2, item.getReservedQuantity());
        assertEquals(8, item.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when reservation quantity exceeds available stock")
    void testInsufficientStock() {
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, "SKU-001", 5);
        assertThrows(InsufficientStockException.class, () -> item.reserve(6));
    }

    @Test
    @DisplayName("Should reject adjusting on-hand quantity below active reserved quantity")
    void testAdjustOnHandBelowReserved() {
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, "SKU-001", 10);
        item.reserve(7);

        assertThrows(IllegalArgumentException.class, () -> item.adjustOnHand(5));
    }

    @Test
    @DisplayName("StockReservation should handle release and expiry transitions correctly")
    void testStockReservationLifecycle() {
        UUID orderId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(15);
        StockReservation reservation = StockReservation.createActiveReservation("RES-001", orderId, expiresAt, List.of());

        assertTrue(reservation.isActive());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());

        reservation.release("CUSTOMER_CANCELLED");
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        assertEquals("CUSTOMER_CANCELLED", reservation.getReleaseReason());
        assertFalse(reservation.isActive());

        // Idempotent release
        reservation.release("CUSTOMER_CANCELLED");
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
    }

    @Test
    @DisplayName("Should reject releasing a non-active reservation")
    void testReleaseNonActiveReservation() {
        UUID orderId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().minusMinutes(5);
        StockReservation reservation = StockReservation.createActiveReservation("RES-002", orderId, expiresAt, List.of());
        reservation.expire();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        assertThrows(ReservationAlreadyReleasedException.class, () -> reservation.release("TEST"));
    }

    @Test
    @DisplayName("InventoryItem commit should reduce both on-hand and reserved quantity")
    void testInventoryItemCommit() {
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, "SKU-COMMIT-001", 20);
        item.reserve(5);

        assertEquals(20, item.getOnHandQuantity());
        assertEquals(5, item.getReservedQuantity());
        assertEquals(15, item.getAvailableQuantity());

        item.commit(5);
        assertEquals(15, item.getOnHandQuantity());
        assertEquals(0, item.getReservedQuantity());
        assertEquals(15, item.getAvailableQuantity());
    }

    @Test
    @DisplayName("InventoryItem recordDamaged should reduce available quantity and calculate low stock")
    void testInventoryItemDamagedAndLowStock() {
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, "SKU-DAMAGED-001", 10);
        assertFalse(item.isLowStock());

        item.recordDamaged(6);
        assertEquals(4, item.getAvailableQuantity());
        assertTrue(item.isLowStock()); // 4 <= lowStockThreshold (5)
    }
}
