package com.sporekart.modules.returns.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnApprovedEvent(
        UUID returnId,
        String returnReference,
        UUID orderId,
        String orderReference,
        String customerId,
        OffsetDateTime occurredAt
) {
    public static ReturnApprovedEvent create(UUID returnId, String returnReference, UUID orderId, String orderReference, String customerId) {
        return new ReturnApprovedEvent(returnId, returnReference, orderId, orderReference, customerId, OffsetDateTime.now());
    }
}
