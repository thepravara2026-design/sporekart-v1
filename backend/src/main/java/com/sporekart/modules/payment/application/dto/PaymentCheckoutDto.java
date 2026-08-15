package com.sporekart.modules.payment.application.dto;

import com.sporekart.modules.payment.domain.PaymentProviderType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCheckoutDto(
        UUID paymentId,
        String paymentReference,
        UUID attemptId,
        String attemptReference,
        UUID orderId,
        BigDecimal amount,
        String currency,
        PaymentProviderType provider,
        String providerOrderId,
        String keyId
) {}
