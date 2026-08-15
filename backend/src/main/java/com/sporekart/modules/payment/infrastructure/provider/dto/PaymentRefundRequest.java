package com.sporekart.modules.payment.infrastructure.provider.dto;

import java.math.BigDecimal;

public record PaymentRefundRequest(
        String paymentReference,
        String providerPaymentId,
        String refundReference,
        BigDecimal amount,
        String currency,
        String reason,
        String idempotencyKey
) {}
