package com.sporekart.modules.grower.web.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record CreateGrowerProductRequestDto(
        String sku,
        String name,
        String description,
        BigDecimal price,
        BigDecimal strikeOutPrice,
        String currency,
        UUID categoryId,
        int initialStockQuantity,
        List<String> imageUrls
) {
    public CreateGrowerProductRequestDto(String sku, String name, String description, BigDecimal price, String currency, UUID categoryId, int initialStockQuantity) {
        this(sku, name, description, price, null, currency, categoryId, initialStockQuantity, Collections.emptyList());
    }

    public CreateGrowerProductRequestDto(String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, UUID categoryId, int initialStockQuantity) {
        this(sku, name, description, price, strikeOutPrice, currency, categoryId, initialStockQuantity, Collections.emptyList());
    }
}
