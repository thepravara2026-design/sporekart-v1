package com.sporekart.modules.payment.infrastructure.provider;

public record PaymentVerificationRequest(
        String providerOrderId,
        String providerPaymentId,
        String providerSignature
) {}
