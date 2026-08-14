package com.sporekart.modules.cart.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Command request payload for adding an item to cart")
public record AddCartItemCommand(
        @Schema(description = "Product ID", example = "f4cfdc7f-e282-458b-9c2f-3d97035b1b09")
        @NotNull(message = "Product ID cannot be null")
        UUID productId,

        @Schema(description = "Optional Product Variant ID", example = "d0d8b0f7-0ea4-410c-9227-76aae84596be")
        UUID variantId,

        @Schema(description = "Quantity to add", example = "2")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
