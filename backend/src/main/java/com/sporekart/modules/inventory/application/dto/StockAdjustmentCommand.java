package com.sporekart.modules.inventory.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StockAdjustmentCommand(
        @NotBlank(message = "SKU is required")
        String sku,

        @Min(value = 0, message = "On-hand quantity cannot be negative")
        int newOnHandQuantity,

        String reason
) {}
