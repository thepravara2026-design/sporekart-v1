package com.sporekart.modules.catalog.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
        @NotBlank(message = "SKU cannot be blank")
        @Size(max = 50, message = "SKU cannot exceed 50 characters")
        String sku,

        @NotBlank(message = "Product name cannot be blank")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        String description,

        @NotNull(message = "Price cannot be null")
        @PositiveOrZero(message = "Price must be non-negative")
        BigDecimal price,

        BigDecimal strikeOutPrice,

        @NotBlank(message = "Currency cannot be blank")
        @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
        String currency,

        UUID categoryId
) {
    public CreateProductCommand(String sku, String name, String description, BigDecimal price, String currency, UUID categoryId) {
        this(sku, name, description, price, null, currency, categoryId);
    }
}
