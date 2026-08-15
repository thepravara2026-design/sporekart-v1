package com.sporekart.modules.payment.infrastructure.provider;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProviderOrderRequest(
        String paymentReference,
        String attemptReference,
        UUID orderId,
        BigDecimal amount,
        String currency,
        String receipt
) {}
