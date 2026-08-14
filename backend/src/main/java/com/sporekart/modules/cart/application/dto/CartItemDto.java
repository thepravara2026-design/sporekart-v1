package com.sporekart.modules.cart.application.dto;

import com.sporekart.modules.cart.domain.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Cart Line Item DTO")
public record CartItemDto(
        @Schema(description = "Item ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Product ID", example = "f4cfdc7f-e282-458b-9c2f-3d97035b1b09")
        UUID productId,

        @Schema(description = "Variant ID", example = "d0d8b0f7-0ea4-410c-9227-76aae84596be")
        UUID variantId,

        @Schema(description = "Product SKU", example = "SHROOM-001")
        String sku,

        @Schema(description = "Product Name Snapshot", example = "Golden Teacher Spore Syringe")
        String productName,

        @Schema(description = "Variant Name Snapshot", example = "10ml Syringe")
        String variantName,

        @Schema(description = "Unit Price Snapshot", example = "29.99")
        BigDecimal unitPrice,

        @Schema(description = "Item Quantity", example = "2")
        int quantity,

        @Schema(description = "Line Total Price", example = "59.98")
        BigDecimal lineTotal,

        @Schema(description = "Item Creation Timestamp")
        OffsetDateTime createdAt,

        @Schema(description = "Item Last Update Timestamp")
        OffsetDateTime updatedAt
) {
    public static CartItemDto fromDomain(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getProductNameSnapshot(),
                item.getVariantNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getQuantity(),
                item.getLineTotal(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
