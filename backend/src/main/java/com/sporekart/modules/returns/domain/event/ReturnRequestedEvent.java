package com.sporekart.modules.returns.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnRequestedEvent(
        UUID returnId,
        String returnReference,
        UUID orderId,
        String orderReference,
        String customerId,
        OffsetDateTime occurredAt
) {
    public static ReturnRequestedEvent create(UUID returnId, String returnReference, UUID orderId, String orderReference, String customerId) {
        return new ReturnRequestedEvent(returnId, returnReference, orderId, orderReference, customerId, OffsetDateTime.now());
    }
}
