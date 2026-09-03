package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TrainingBatchCreatedEvent implements TrainingEvent {
    private final String eventId;
    private final String batchId;
    private final String programId;
    private final String batchCode;
    private final int totalCapacity;
    private final Instant occurredAt;

    public TrainingBatchCreatedEvent(String batchId, String programId, String batchCode, int totalCapacity) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.programId = programId;
        this.batchCode = batchCode;
        this.totalCapacity = totalCapacity;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "TRAINING_BATCH_CREATED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public String getProgramId() { return programId; }
    public String getBatchCode() { return batchCode; }
    public int getTotalCapacity() { return totalCapacity; }
}
