package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class BatchCapacityChangedEvent implements TrainingEvent {
    private final String eventId;
    private final String batchId;
    private final int previousCapacity;
    private final int newCapacity;
    private final Instant occurredAt;

    public BatchCapacityChangedEvent(String batchId, int previousCapacity, int newCapacity) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.previousCapacity = previousCapacity;
        this.newCapacity = newCapacity;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "BATCH_CAPACITY_CHANGED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public int getPreviousCapacity() { return previousCapacity; }
    public int getNewCapacity() { return newCapacity; }
}
