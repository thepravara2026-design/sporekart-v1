package com.sporekart.modules.training.domain;

import com.sporekart.modules.training.domain.exception.InvalidPaymentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingEnrollmentPayment {

    private final String id;
    private final String paymentId;
    private final String batchId;
    private final String traineeId;
    private String enrollmentId;
    private final BigDecimal amount;
    private final String currency;
    private TrainingPaymentStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final String createdBy;
    private String updatedBy;

    public TrainingEnrollmentPayment(
            String id,
            String paymentId,
            String batchId,
            String traineeId,
            String enrollmentId,
            BigDecimal amount,
            String currency,
            TrainingPaymentStatus status,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId must not be null");
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.enrollmentId = enrollmentId;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Payment amount must not be negative");
        }
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status != null ? status : TrainingPaymentStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
        this.updatedBy = updatedBy != null ? updatedBy : "SYSTEM";
    }

    public static TrainingEnrollmentPayment create(
            String paymentId,
            String batchId,
            String traineeId,
            BigDecimal amount,
            String currency,
            String createdBy
    ) {
        return new TrainingEnrollmentPayment(
                UUID.randomUUID().toString(),
                paymentId,
                batchId,
                traineeId,
                null,
                amount,
                currency,
                TrainingPaymentStatus.PENDING,
                Instant.now(),
                Instant.now(),
                createdBy,
                createdBy
        );
    }

    public void markVerified(String updaterId) {
        if (this.status == TrainingPaymentStatus.ENROLLMENT_CONFIRMED) {
            return;
        }
        this.status = TrainingPaymentStatus.VERIFIED;
        this.updatedAt = Instant.now();
        if (updaterId != null) this.updatedBy = updaterId;
    }

    public void markEnrollmentConfirmed(String enrollmentId, String updaterId) {
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.status = TrainingPaymentStatus.ENROLLMENT_CONFIRMED;
        this.updatedAt = Instant.now();
        if (updaterId != null) this.updatedBy = updaterId;
    }

    public void markEnrollmentPending(String updaterId) {
        if (this.status == TrainingPaymentStatus.ENROLLMENT_CONFIRMED) {
            throw new InvalidPaymentStateException("Cannot mark enrollment pending for an already confirmed payment");
        }
        this.status = TrainingPaymentStatus.ENROLLMENT_PENDING;
        this.updatedAt = Instant.now();
        if (updaterId != null) this.updatedBy = updaterId;
    }

    public void markFailed(String updaterId) {
        if (this.status == TrainingPaymentStatus.ENROLLMENT_CONFIRMED) {
            throw new InvalidPaymentStateException("Cannot mark confirmed training payment as failed");
        }
        this.status = TrainingPaymentStatus.FAILED;
        this.updatedAt = Instant.now();
        if (updaterId != null) this.updatedBy = updaterId;
    }

    // Getters
    public String getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public String getEnrollmentId() { return enrollmentId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TrainingPaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
}
