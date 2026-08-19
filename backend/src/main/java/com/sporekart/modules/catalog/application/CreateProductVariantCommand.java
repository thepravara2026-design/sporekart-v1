package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.product.QuantityUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateProductVariantCommand(
        String sku,

        @NotNull(message = "Variant quantity value cannot be null")
        @Positive(message = "Variant quantity value must be strictly positive")
        BigDecimal quantityValue,

        @NotNull(message = "Variant quantity unit cannot be null")
        QuantityUnit quantityUnit,

        @NotNull(message = "Variant selling price cannot be null")
        @PositiveOrZero(message = "Variant selling price must be non-negative")
        BigDecimal sellingPrice,

        BigDecimal strikeOutPrice
) {}
