package com.sporekart.modules.returns.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnRejectedEvent(
        UUID returnId,
        String returnReference,
        UUID orderId,
        String orderReference,
        String customerId,
        String reason,
        OffsetDateTime occurredAt
) {
    public static ReturnRejectedEvent create(UUID returnId, String returnReference, UUID orderId, String orderReference, String customerId, String reason) {
        return new ReturnRejectedEvent(returnId, returnReference, orderId, orderReference, customerId, reason, OffsetDateTime.now());
    }
}
