package com.sporekart.modules.payment.application.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentVerificationCommand(
        @NotBlank(message = "Payment reference is required")
        String paymentReference,

        @NotBlank(message = "Provider order ID is required")
        String providerOrderId,

        @NotBlank(message = "Provider payment ID is required")
        String providerPaymentId,

        @NotBlank(message = "Provider signature is required")
        String providerSignature
) {}
