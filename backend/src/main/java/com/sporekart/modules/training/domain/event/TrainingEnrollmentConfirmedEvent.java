package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.Objects;

public class TrainingEnrollmentConfirmedEvent {
    private final String enrollmentId;
    private final String batchId;
    private final String traineeId;
    private final String paymentReference;
    private final Instant confirmedAt;

    public TrainingEnrollmentConfirmedEvent(String enrollmentId, String batchId, String traineeId, String paymentReference) {
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.paymentReference = paymentReference;
        this.confirmedAt = Instant.now();
    }

    public String getEnrollmentId() { return enrollmentId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getConfirmedAt() { return confirmedAt; }
}
