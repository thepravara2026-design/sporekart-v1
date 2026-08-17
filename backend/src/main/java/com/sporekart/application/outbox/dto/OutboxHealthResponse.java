package com.sporekart.application.outbox.dto;

import java.time.OffsetDateTime;

public record OutboxHealthResponse(
        long pendingCount,
        long processingCount,
        long processedCount,
        long failedCount,
        long deadCount,
        long totalCount,
        OffsetDateTime oldestPendingAt,
        OffsetDateTime oldestFailedAt,
        OffsetDateTime oldestDeadAt,
        String status
) {
    public static OutboxHealthResponse of(
            long pending,
            long processing,
            long processed,
            long failed,
            long dead,
            OffsetDateTime oldestPending,
            OffsetDateTime oldestFailed,
            OffsetDateTime oldestDead
    ) {
        long total = pending + processing + processed + failed + dead;
        String systemStatus = (dead > 0 || failed > 10) ? "DEGRADED" : "HEALTHY";
        return new OutboxHealthResponse(
                pending, processing, processed, failed, dead, total,
                oldestPending, oldestFailed, oldestDead, systemStatus
        );
    }
}
