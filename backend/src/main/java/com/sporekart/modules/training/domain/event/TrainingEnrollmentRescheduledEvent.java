package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TrainingEnrollmentRescheduledEvent implements TrainingEvent {
    private final String eventId;
    private final String enrollmentId;
    private final String sourceBatchId;
    private final String targetBatchId;
    private final String traineeId;
    private final Instant occurredAt;

    public TrainingEnrollmentRescheduledEvent(String enrollmentId, String sourceBatchId, String targetBatchId, String traineeId) {
        this.eventId = UUID.randomUUID().toString();
        this.enrollmentId = enrollmentId;
        this.sourceBatchId = sourceBatchId;
        this.targetBatchId = targetBatchId;
        this.traineeId = traineeId;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "TRAINING_ENROLLMENT_RESCHEDULED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getEnrollmentId() { return enrollmentId; }
    public String getSourceBatchId() { return sourceBatchId; }
    public String getTargetBatchId() { return targetBatchId; }
    public String getTraineeId() { return traineeId; }
}
