package com.sporekart.modules.training.domain.event;

import com.sporekart.modules.training.domain.EnrollmentStatus;

import java.time.Instant;
import java.util.Objects;

public class TrainingEnrollmentStatusChangedEvent {
    private final String enrollmentId;
    private final String batchId;
    private final String traineeId;
    private final EnrollmentStatus previousStatus;
    private final EnrollmentStatus newStatus;
    private final Instant occurredAt;

    public TrainingEnrollmentStatusChangedEvent(String enrollmentId, String batchId, String traineeId, EnrollmentStatus previousStatus, EnrollmentStatus newStatus) {
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus, "newStatus must not be null");
        this.occurredAt = Instant.now();
    }

    public String getEnrollmentId() { return enrollmentId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public EnrollmentStatus getPreviousStatus() { return previousStatus; }
    public EnrollmentStatus getNewStatus() { return newStatus; }
    public Instant getOccurredAt() { return occurredAt; }
}
