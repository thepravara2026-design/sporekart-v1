package com.sporekart.modules.inventory.application.dto;

import com.sporekart.modules.inventory.domain.InventoryItem;

public record StockAvailabilityDto(
        String sku,
        String status,
        boolean available,
        int lowStockThreshold
) {
    public static StockAvailabilityDto fromDomain(InventoryItem item) {
        int avail = item.getAvailableQuantity();
        String status;
        if (avail <= 0) {
            status = "OUT_OF_STOCK";
        } else if (item.isLowStock()) {
            status = "LOW_STOCK";
        } else {
            status = "IN_STOCK";
        }
        return new StockAvailabilityDto(item.getSku(), status, avail > 0, item.getLowStockThreshold());
    }
}
