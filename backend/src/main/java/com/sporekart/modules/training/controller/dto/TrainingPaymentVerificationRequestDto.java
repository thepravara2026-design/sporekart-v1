package com.sporekart.modules.training.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainingPaymentVerificationRequestDto(
        @NotBlank(message = "paymentReference is required")
        String paymentReference,

        @NotBlank(message = "providerOrderId is required")
        String providerOrderId,

        @NotBlank(message = "providerPaymentId is required")
        String providerPaymentId,

        @NotBlank(message = "providerSignature is required")
        String providerSignature
) {}
