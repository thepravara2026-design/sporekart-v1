package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class BatchActivatedEvent implements TrainingEvent {
    private final String eventId;
    private final String batchId;
    private final String batchCode;
    private final Instant occurredAt;

    public BatchActivatedEvent(String batchId, String batchCode) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "BATCH_ACTIVATED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public String getBatchCode() { return batchCode; }
}
