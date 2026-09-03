package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TrainingEnrollmentCreatedEvent implements TrainingEvent {
    private final String eventId;
    private final String enrollmentId;
    private final String batchId;
    private final String traineeId;
    private final Instant occurredAt;

    public TrainingEnrollmentCreatedEvent(String enrollmentId, String batchId, String traineeId) {
        this.eventId = UUID.randomUUID().toString();
        this.enrollmentId = enrollmentId;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "TRAINING_ENROLLMENT_CREATED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getEnrollmentId() { return enrollmentId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
}
