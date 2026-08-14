package com.sporekart.modules.catalog.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        UUID categoryId
) {}
