package com.sporekart.modules.checkout.application.dto;

import com.sporekart.modules.checkout.domain.model.CheckoutWarning;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Warning issued during checkout preview calculation")
public record CheckoutWarningResponse(
        String type,
        UUID productId,
        String message
) {
    public static CheckoutWarningResponse fromDomain(CheckoutWarning warning) {
        return new CheckoutWarningResponse(
                warning.getType().name(),
                warning.getProductId(),
                warning.getMessage()
        );
    }
}
