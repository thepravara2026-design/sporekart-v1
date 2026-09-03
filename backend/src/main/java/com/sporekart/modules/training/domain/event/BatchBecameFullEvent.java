package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class BatchBecameFullEvent implements TrainingEvent {

    private final String eventId;
    private final String batchId;
    private final String batchCode;
    private final int totalCapacity;
    private final Instant occurredAt;

    public BatchBecameFullEvent(String batchId, String batchCode, int totalCapacity) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.totalCapacity = totalCapacity;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "BATCH_BECAME_FULL"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public String getBatchCode() { return batchCode; }
    public int getTotalCapacity() { return totalCapacity; }
}
