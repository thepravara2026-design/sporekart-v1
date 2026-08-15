package com.sporekart.modules.payment.infrastructure.provider.dto;

import java.math.BigDecimal;

public record PaymentRefundResult(
        boolean success,
        String providerRefundId,
        BigDecimal amount,
        String currency,
        String rawResponse,
        String failureReason
) {}
