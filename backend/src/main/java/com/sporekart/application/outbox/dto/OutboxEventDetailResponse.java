package com.sporekart.application.outbox.dto;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;

import java.time.OffsetDateTime;

public record OutboxEventDetailResponse(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payloadPreview,
        OutboxStatus status,
        int retryCount,
        int maxRetries,
        String lastError,
        OffsetDateTime createdAt,
        OffsetDateTime scheduledAt,
        OffsetDateTime processedAt,
        Long version
) {
    public static OutboxEventDetailResponse fromEntity(OutboxEvent event) {
        String payload = event.getPayload();
        String preview = payload;
        if (payload != null && payload.length() > 500) {
            preview = payload.substring(0, 500) + "... [truncated for operational security]";
        }
        return new OutboxEventDetailResponse(
                event.getId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                preview,
                event.getStatus(),
                event.getRetryCount(),
                event.getMaxRetries(),
                event.getLastError(),
                event.getCreatedAt(),
                event.getScheduledAt(),
                event.getProcessedAt(),
                event.getVersion()
        );
    }
}
