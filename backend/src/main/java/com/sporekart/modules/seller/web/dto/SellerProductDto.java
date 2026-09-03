package com.sporekart.modules.seller.web.dto;

import java.math.BigDecimal;

public record SellerProductDto(
        String id,
        String name,
        String sku,
        String category,
        BigDecimal price,
        BigDecimal strikeOutPrice,
        String currency,
        int onHandQuantity,
        int reservedQuantity,
        String syncStatus,
        String status,
        String updatedAt
) {}
