package com.sporekart.modules.inventory.application.dto;

import com.sporekart.modules.inventory.domain.StockReservationItem;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservationItemDto(
        UUID id,
        UUID reservationId,
        UUID inventoryItemId,
        UUID productId,
        UUID variantId,
        String sku,
        int quantity,
        OffsetDateTime createdAt
) {
    public static ReservationItemDto fromDomain(StockReservationItem item) {
        return new ReservationItemDto(
                item.getId(),
                item.getReservationId(),
                item.getInventoryItemId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getQuantity(),
                item.getCreatedAt()
        );
    }
}
