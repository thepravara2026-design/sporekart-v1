package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingEnrollmentCompletedEvent {

    private final String eventId;
    private final String enrollmentId;
    private final String batchId;
    private final String traineeId;
    private final String certificateId;
    private final Instant completedAt;

    public TrainingEnrollmentCompletedEvent(String enrollmentId, String batchId, String traineeId, String certificateId) {
        this.eventId = UUID.randomUUID().toString();
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.certificateId = certificateId;
        this.completedAt = Instant.now();
    }

    public String getEventId() { return eventId; }
    public String getEnrollmentId() { return enrollmentId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public String getCertificateId() { return certificateId; }
    public Instant getCompletedAt() { return completedAt; }
}
