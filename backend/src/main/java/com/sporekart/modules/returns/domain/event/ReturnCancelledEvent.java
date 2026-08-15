package com.sporekart.modules.returns.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnCancelledEvent(
        UUID returnId,
        String returnReference,
        UUID orderId,
        String orderReference,
        String customerId,
        OffsetDateTime occurredAt
) {
    public static ReturnCancelledEvent create(UUID returnId, String returnReference, UUID orderId, String orderReference, String customerId) {
        return new ReturnCancelledEvent(returnId, returnReference, orderId, orderReference, customerId, OffsetDateTime.now());
    }
}
