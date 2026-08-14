package com.sporekart.modules.cart.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Command request payload for updating cart item quantity")
public record UpdateCartItemCommand(
        @Schema(description = "New quantity for line item", example = "3")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
