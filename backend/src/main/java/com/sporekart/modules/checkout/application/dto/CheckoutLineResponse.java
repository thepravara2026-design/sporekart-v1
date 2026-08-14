package com.sporekart.modules.checkout.application.dto;

import com.sporekart.modules.checkout.domain.model.CheckoutLineItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Calculated line item details in a checkout preview")
public record CheckoutLineResponse(
        UUID cartItemId,
        UUID productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal cartUnitPrice,
        BigDecimal authoritativeUnitPrice,
        boolean priceChanged,
        BigDecimal lineSubtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {
    public static CheckoutLineResponse fromDomain(CheckoutLineItem item) {
        return new CheckoutLineResponse(
                item.getCartItemId(),
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getCartUnitPrice().getAmount(),
                item.getAuthoritativeUnitPrice().getAmount(),
                item.isPriceChanged(),
                item.getLineSubtotal().getAmount(),
                item.getDiscountAmount().getAmount(),
                item.getTaxAmount().getAmount(),
                item.getLineTotal().getAmount()
        );
    }
}
