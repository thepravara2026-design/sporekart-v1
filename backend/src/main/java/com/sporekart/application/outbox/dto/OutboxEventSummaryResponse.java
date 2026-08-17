package com.sporekart.application.outbox.dto;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;

import java.time.OffsetDateTime;

public record OutboxEventSummaryResponse(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        OutboxStatus status,
        int retryCount,
        int maxRetries,
        String lastError,
        OffsetDateTime createdAt,
        OffsetDateTime scheduledAt,
        OffsetDateTime processedAt
) {
    public static OutboxEventSummaryResponse fromEntity(OutboxEvent event) {
        return new OutboxEventSummaryResponse(
                event.getId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                event.getStatus(),
                event.getRetryCount(),
                event.getMaxRetries(),
                event.getLastError(),
                event.getCreatedAt(),
                event.getScheduledAt(),
                event.getProcessedAt()
        );
    }
}
