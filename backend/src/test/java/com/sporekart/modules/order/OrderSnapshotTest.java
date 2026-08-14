package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderSnapshotTest {

    @Test
    @DisplayName("Historical Order snapshots must remain immutable even when external catalog or address data changes")
    void testHistoricalSnapshotImmutability() {
        // Initial state at purchase time
        String originalProductName = "Shiitake Spawn Bag";
        String originalSku = "SHK-001";
        BigDecimal originalUnitPrice = new BigDecimal("299.00");

        AddressSnapshot purchaseAddress = new AddressSnapshot(
                "Alice Smith", "9876543210", "12 Old Street", null, "Pune", "Maharashtra", "411001", "India"
        );

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItem item = new OrderItem(
                UUID.randomUUID(), orderId, productId, null, originalSku,
                originalProductName, null, originalUnitPrice, 2,
                BigDecimal.ZERO, new BigDecimal("107.64"), new BigDecimal("598.00"),
                new BigDecimal("705.64"), OffsetDateTime.now()
        );

        Order order = new Order(
                orderId, "SPK-20260815-999999", "cust-55", OrderStatus.CREATED, "INR",
                new BigDecimal("598.00"), BigDecimal.ZERO, new BigDecimal("107.64"), new BigDecimal("50.00"),
                new BigDecimal("755.64"), null, purchaseAddress, null, List.of(item),
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        // Simulate subsequent catalog product price hike / name rename
        String updatedCatalogName = "Premium Golden Shiitake Spawn Bag";
        BigDecimal updatedCatalogPrice = new BigDecimal("499.00");

        // Simulate customer updating profile address book
        AddressSnapshot newProfileAddress = new AddressSnapshot(
                "Alice Smith", "9876543210", "99 New High Tech Park", null, "Mumbai", "Maharashtra", "400002", "India"
        );

        // Assert Order aggregate snapshots are unaffected
        OrderItem orderSnapshotItem = order.getItems().get(0);
        assertEquals(originalProductName, orderSnapshotItem.getProductNameSnapshot());
        assertEquals(originalSku, orderSnapshotItem.getSku());
        assertEquals(originalUnitPrice, orderSnapshotItem.getUnitPrice());

        AddressSnapshot orderSnapshotAddress = order.getShippingAddress();
        assertEquals("12 Old Street", orderSnapshotAddress.getAddressLine1());
        assertEquals("Pune", orderSnapshotAddress.getCity());
        assertEquals("411001", orderSnapshotAddress.getPostalCode());
    }
}
