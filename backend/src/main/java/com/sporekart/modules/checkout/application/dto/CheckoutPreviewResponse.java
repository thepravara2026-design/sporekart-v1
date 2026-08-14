package com.sporekart.modules.checkout.application.dto;

import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Authoritative server-calculated checkout preview response")
public record CheckoutPreviewResponse(
        UUID previewId,
        UUID cartId,
        String customerId,
        String currency,
        List<CheckoutLineResponse> items,
        PriceBreakdownResponse breakdown,
        List<CheckoutWarningResponse> warnings,
        OffsetDateTime generatedAt
) {
    public static CheckoutPreviewResponse fromDomain(CheckoutPreview preview) {
        List<CheckoutLineResponse> lineResponses = preview.getItems().stream()
                .map(CheckoutLineResponse::fromDomain)
                .toList();

        List<CheckoutWarningResponse> warningResponses = preview.getWarnings().stream()
                .map(CheckoutWarningResponse::fromDomain)
                .toList();

        PriceBreakdownResponse breakdown = new PriceBreakdownResponse(
                preview.getSubtotal().getAmount(),
                preview.getDiscountTotal().getAmount(),
                preview.getTaxTotal().getAmount(),
                preview.getShippingFee().getAmount(),
                preview.getGrandTotal().getAmount(),
                preview.getCurrency()
        );

        return new CheckoutPreviewResponse(
                preview.getPreviewId(),
                preview.getCartId(),
                preview.getCustomerId(),
                preview.getCurrency(),
                lineResponses,
                breakdown,
                warningResponses,
                preview.getGeneratedAt()
        );
    }
}
