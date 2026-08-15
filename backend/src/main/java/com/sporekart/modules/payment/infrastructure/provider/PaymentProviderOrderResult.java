package com.sporekart.modules.payment.infrastructure.provider;

import java.math.BigDecimal;

public record PaymentProviderOrderResult(
        String providerOrderId,
        String providerPaymentId,
        BigDecimal amount,
        String currency,
        String rawResponse
) {}
