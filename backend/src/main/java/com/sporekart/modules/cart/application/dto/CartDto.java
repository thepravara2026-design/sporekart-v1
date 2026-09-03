package com.sporekart.modules.cart.application.dto;

import com.sporekart.modules.cart.domain.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Customer Active Cart DTO")
public record CartDto(
        @Schema(description = "Cart ID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        UUID id,

        @Schema(description = "Customer Identity ID", example = "customer-123")
        String customerId,

        @Schema(description = "Cart Status", example = "ACTIVE")
        String status,

        @Schema(description = "Currency Code", example = "INR")
        String currency,

        @Schema(description = "Cart Subtotal Amount", example = "59.98")
        BigDecimal subtotal,

        @Schema(description = "Total Quantity of Items", example = "2")
        int itemCount,

        @Schema(description = "List of Cart Items")
        List<CartItemDto> items,

        @Schema(description = "Cart Creation Timestamp")
        OffsetDateTime createdAt,

        @Schema(description = "Cart Last Update Timestamp")
        OffsetDateTime updatedAt
) {
    public static CartDto fromDomain(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(CartItemDto::fromDomain)
                .toList();

        return new CartDto(
                cart.getId(),
                cart.getCustomerId(),
                cart.getStatus().name(),
                cart.getCurrency(),
                cart.getSubtotal(),
                cart.getItemCount(),
                itemDtos,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
