package com.sporekart.modules.returns.domain.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReturnAcceptedEvent(
        UUID returnId,
        String returnReference,
        UUID orderId,
        String orderReference,
        String customerId,
        BigDecimal totalRefundAmount,
        List<AcceptedItemPayload> acceptedItems,
        OffsetDateTime occurredAt
) {
    public record AcceptedItemPayload(UUID orderItemId, UUID productId, String sku, int acceptedQuantity) {}

    public static ReturnAcceptedEvent create(
            UUID returnId,
            String returnReference,
            UUID orderId,
            String orderReference,
            String customerId,
            BigDecimal totalRefundAmount,
            List<AcceptedItemPayload> acceptedItems
    ) {
        return new ReturnAcceptedEvent(returnId, returnReference, orderId, orderReference, customerId, totalRefundAmount, acceptedItems, OffsetDateTime.now());
    }
}
