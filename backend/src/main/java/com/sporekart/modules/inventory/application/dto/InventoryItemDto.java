package com.sporekart.modules.inventory.application.dto;

import com.sporekart.modules.inventory.domain.InventoryItem;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryItemDto(
        UUID id,
        UUID productId,
        UUID variantId,
        String sku,
        int onHandQuantity,
        int reservedQuantity,
        int availableQuantity,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static InventoryItemDto fromDomain(InventoryItem item) {
        return new InventoryItemDto(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getOnHandQuantity(),
                item.getReservedQuantity(),
                item.getAvailableQuantity(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
