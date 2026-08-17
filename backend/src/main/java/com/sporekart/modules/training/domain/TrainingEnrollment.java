package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingEnrollment {

    private final String id;
    private final String batchId;
    private final String traineeId;
    private EnrollmentStatus status;
    private String paymentReference;
    private final Instant createdAt;
    private Instant updatedAt;

    public TrainingEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.status = status != null ? status : EnrollmentStatus.PENDING;
        this.paymentReference = paymentReference;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static TrainingEnrollment create(String batchId, String traineeId) {
        return new TrainingEnrollment(null, batchId, traineeId, EnrollmentStatus.PENDING, null, Instant.now(), Instant.now());
    }

    public void confirm(String paymentReference) {
        this.status = EnrollmentStatus.CONFIRMED;
        if (paymentReference != null) {
            this.paymentReference = paymentReference;
        }
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void waitlist() {
        this.status = EnrollmentStatus.WAITLISTED;
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public EnrollmentStatus getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingEnrollment that = (TrainingEnrollment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
