package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class BatchScheduledEvent implements TrainingEvent {
    private final String eventId;
    private final String batchId;
    private final String programId;
    private final String batchCode;
    private final Instant startDate;
    private final Instant endDate;
    private final Instant occurredAt;

    public BatchScheduledEvent(String batchId, String programId, String batchCode, Instant startDate, Instant endDate) {
        this.eventId = UUID.randomUUID().toString();
        this.batchId = batchId;
        this.programId = programId;
        this.batchCode = batchCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "BATCH_SCHEDULED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getBatchId() { return batchId; }
    public String getProgramId() { return programId; }
    public String getBatchCode() { return batchCode; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
}
