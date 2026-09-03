package com.sporekart.modules.seller.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AdjustSellerStockRequestDto(
        @NotBlank(message = "SKU is required")
        String sku,

        @Min(value = 0, message = "New on-hand quantity cannot be negative")
        int newOnHandQuantity,

        String reason
) {}
