package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class BatchCapacityAvailableEvent implements TrainingEvent {

    private final String eventId;
    private final String batchId;
    private final String batchCode;
    private final int totalCapacity;
    private final int occupiedSeats;
    private final Instant occurredAt;

    public BatchCapacityAvailableEvent(String batchId, String batchCode, int totalCapacity, int occupiedSeats) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.totalCapacity = totalCapacity;
        this.occupiedSeats = occupiedSeats;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "BATCH_CAPACITY_AVAILABLE"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public String getBatchCode() { return batchCode; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getOccupiedSeats() { return occupiedSeats; }
}
