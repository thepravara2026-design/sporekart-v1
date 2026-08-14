package com.sporekart.modules.checkout.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Optional inputs for calculating a server-authoritative checkout preview")
public record CheckoutPreviewRequest(
        @Schema(description = "Optional destination address for shipping estimation", example = "Mumbai, Maharashtra 400001")
        String destinationAddress,

        @Schema(description = "Optional promo or coupon code", example = "WELCOME10")
        String couponCode
) {}
