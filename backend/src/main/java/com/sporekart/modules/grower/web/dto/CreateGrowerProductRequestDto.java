package com.sporekart.modules.grower.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateGrowerProductRequestDto(
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        UUID categoryId,
        int initialStockQuantity
) {}
