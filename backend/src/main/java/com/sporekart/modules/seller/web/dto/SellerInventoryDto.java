package com.sporekart.modules.seller.web.dto;

public record SellerInventoryDto(
        String id,
        String sku,
        String productName,
        String warehouseLocation,
        int onHandQuantity,
        int reservedQuantity,
        int availableQuantity,
        String syncStatus,
        String lastSyncedAt
) {}
