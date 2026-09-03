package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class BatchCapacityChangedEvent implements TrainingEvent {

    private final String eventId;
    private final String batchId;
    private final String batchCode;
    private final int previousCapacity;
    private final int newCapacity;
    private final int occupiedSeats;
    private final Instant occurredAt;

    public BatchCapacityChangedEvent(String batchId, String batchCode, int previousCapacity, int newCapacity, int occupiedSeats) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.previousCapacity = previousCapacity;
        this.newCapacity = newCapacity;
        this.occupiedSeats = occupiedSeats;
        this.occurredAt = Instant.now();
    }

    public BatchCapacityChangedEvent(String batchId, int previousCapacity, int newCapacity) {
        this(batchId, batchId, previousCapacity, newCapacity, 0);
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "BATCH_CAPACITY_CHANGED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public String getBatchCode() { return batchCode; }
    public int getPreviousCapacity() { return previousCapacity; }
    public int getNewCapacity() { return newCapacity; }
    public int getOccupiedSeats() { return occupiedSeats; }
}
