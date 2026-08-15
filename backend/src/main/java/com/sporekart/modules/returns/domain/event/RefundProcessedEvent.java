package com.sporekart.modules.returns.domain.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RefundProcessedEvent(
        UUID refundId,
        String refundReference,
        UUID returnId,
        String returnReference,
        UUID orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String providerRefundId,
        String idempotencyKey,
        OffsetDateTime occurredAt
) {
    public static RefundProcessedEvent create(
            UUID refundId,
            String refundReference,
            UUID returnId,
            String returnReference,
            UUID orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            String providerRefundId,
            String idempotencyKey
    ) {
        return new RefundProcessedEvent(
                refundId, refundReference, returnId, returnReference, orderId, customerId, amount, currency, providerRefundId, idempotencyKey, OffsetDateTime.now()
        );
    }
}
