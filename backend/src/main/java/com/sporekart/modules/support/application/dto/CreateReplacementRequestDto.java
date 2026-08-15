package com.sporekart.modules.support.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReplacementRequestDto(
        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "Order reference is required")
        String orderReference,

        @NotBlank(message = "Order item ID is required")
        String orderItemId,

        @NotBlank(message = "Product ID is required")
        String productId,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @NotBlank(message = "Reason is required")
        String reason
) {}
