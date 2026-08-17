package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "training_enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_batch_trainee", columnNames = {"batch_id", "trainee_id"})
})
public class TrainingEnrollmentEntity {

    @Id
    private String id;

    @Column(name = "enrollment_code", unique = true)
    private String enrollmentCode;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "trainee_id", nullable = false)
    private String traineeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "price_amount", nullable = false)
    private BigDecimal priceAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    protected TrainingEnrollmentEntity() {}

    public TrainingEnrollmentEntity(String id, String enrollmentCode, String batchId, String traineeId, EnrollmentStatus status,
                                    String paymentReference, BigDecimal priceAmount, String currency, Instant enrolledAt,
                                    Instant confirmedAt, Instant activatedAt, Instant completedAt, Instant createdAt, Instant updatedAt,
                                    String createdBy, String updatedBy, String idempotencyKey) {
        this.id = id;
        this.enrollmentCode = enrollmentCode;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.status = status;
        this.paymentReference = paymentReference;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.enrolledAt = enrolledAt;
        this.confirmedAt = confirmedAt;
        this.activatedAt = activatedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.idempotencyKey = idempotencyKey;
    }

    public static TrainingEnrollmentEntity fromDomain(TrainingEnrollment domain) {
        return new TrainingEnrollmentEntity(
                domain.getId(),
                domain.getEnrollmentCode(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getStatus(),
                domain.getPaymentReference(),
                domain.getPriceAmount(),
                domain.getCurrency(),
                domain.getEnrolledAt(),
                domain.getConfirmedAt(),
                domain.getActivatedAt(),
                domain.getCompletedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getCreatedBy(),
                domain.getUpdatedBy(),
                domain.getIdempotencyKey()
        );
    }

    public TrainingEnrollment toDomain() {
        return new TrainingEnrollment(
                id, enrollmentCode, batchId, traineeId, status, paymentReference,
                priceAmount, currency, enrolledAt, confirmedAt, activatedAt, completedAt,
                createdAt, updatedAt, createdBy, updatedBy, idempotencyKey
        );
    }

    // Getters
    public String getId() { return id; }
    public String getEnrollmentCode() { return enrollmentCode; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public EnrollmentStatus getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getActivatedAt() { return activatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
