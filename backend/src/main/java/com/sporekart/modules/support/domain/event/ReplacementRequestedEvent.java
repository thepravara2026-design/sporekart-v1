package com.sporekart.modules.support.domain.event;

import java.time.OffsetDateTime;

public record ReplacementRequestedEvent(
        String replacementId,
        String replacementReference,
        String ticketId,
        String orderId,
        String sku,
        int quantity,
        OffsetDateTime occurredAt
) {
    public static ReplacementRequestedEvent create(
            String replacementId,
            String replacementReference,
            String ticketId,
            String orderId,
            String sku,
            int quantity
    ) {
        return new ReplacementRequestedEvent(replacementId, replacementReference, ticketId, orderId, sku, quantity, OffsetDateTime.now());
    }
}
