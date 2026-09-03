package com.sporekart.modules.seller.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateSellerProductRequestDto(
        @NotBlank(message = "Product name is required")
        String name,

        @NotBlank(message = "SKU is required")
        String sku,

        String category,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        BigDecimal strikeOutPrice,

        int initialStock
) {}
