package com.sporekart.modules.catalog.application;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductCommand(
        String name,
        String description,
        BigDecimal price,
        BigDecimal strikeOutPrice,
        String currency,
        UUID categoryId
) {
    public UpdateProductCommand(String name, String description, BigDecimal price, String currency, UUID categoryId) {
        this(name, description, price, null, currency, categoryId);
    }
}
