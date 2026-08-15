package com.sporekart.modules.payment.infrastructure.provider;

import com.sporekart.modules.payment.domain.PaymentStatus;

import java.math.BigDecimal;

public record PaymentStatusResult(
        String providerPaymentId,
        String providerOrderId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String failureCode,
        String failureReason
) {}
