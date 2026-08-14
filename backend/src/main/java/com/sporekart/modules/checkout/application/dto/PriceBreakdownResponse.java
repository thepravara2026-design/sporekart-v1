package com.sporekart.modules.checkout.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Server-authoritative price breakdown summary")
public record PriceBreakdownResponse(
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal taxTotal,
        BigDecimal shippingFee,
        BigDecimal grandTotal,
        String currency
) {}
