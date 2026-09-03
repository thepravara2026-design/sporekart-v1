package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "training_enrollment_payments")
public class TrainingEnrollmentPaymentEntity {

    @Id
    private String id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "trainee_id", nullable = false)
    private String traineeId;

    @Column(name = "enrollment_id")
    private String enrollmentId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TrainingPaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    protected TrainingEnrollmentPaymentEntity() {}

    public TrainingEnrollmentPaymentEntity(
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
        this.id = id;
        this.paymentId = paymentId;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.enrollmentId = enrollmentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public static TrainingEnrollmentPaymentEntity fromDomain(TrainingEnrollmentPayment domain) {
        return new TrainingEnrollmentPaymentEntity(
                domain.getId(),
                domain.getPaymentId(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getEnrollmentId(),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getCreatedBy(),
                domain.getUpdatedBy()
        );
    }

    public TrainingEnrollmentPayment toDomain() {
        return new TrainingEnrollmentPayment(
                id,
                paymentId,
                batchId,
                traineeId,
                enrollmentId,
                amount,
                currency,
                status,
                createdAt,
                updatedAt,
                createdBy,
                updatedBy
        );
    }

    // Getters & Setters
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
