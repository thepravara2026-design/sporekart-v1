package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TrainingEnrollmentCancelledEvent implements TrainingEvent {
    private final String eventId;
    private final String enrollmentId;
    private final String batchId;
    private final String traineeId;
    private final String cancelledByRole;
    private final Instant occurredAt;

    public TrainingEnrollmentCancelledEvent(String enrollmentId, String batchId, String traineeId, String cancelledByRole) {
        this.eventId = UUID.randomUUID().toString();
        this.enrollmentId = enrollmentId;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.cancelledByRole = cancelledByRole;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "TRAINING_ENROLLMENT_CANCELLED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getEnrollmentId() { return enrollmentId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public String getCancelledByRole() { return cancelledByRole; }
}
