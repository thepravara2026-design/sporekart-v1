package com.sporekart.modules.training.domain;

import com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingEnrollment {

    private final String id;
    private final String enrollmentCode;
    private final String batchId;
    private final String traineeId;
    private EnrollmentStatus status;
    private String paymentReference;
    private BigDecimal priceAmount;
    private String currency;
    private final Instant enrolledAt;
    private Instant confirmedAt;
    private Instant activatedAt;
    private Instant completedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private String idempotencyKey;

    public TrainingEnrollment(String id, String enrollmentCode, String batchId, String traineeId, EnrollmentStatus status,
                              String paymentReference, BigDecimal priceAmount, String currency, Instant enrolledAt,
                              Instant confirmedAt, Instant activatedAt, Instant completedAt, Instant createdAt, Instant updatedAt,
                              String createdBy, String updatedBy, String idempotencyKey) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.enrollmentCode = enrollmentCode != null ? enrollmentCode : generateEnrollmentCode(this.id);
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.status = status != null ? status : EnrollmentStatus.PENDING;
        this.paymentReference = paymentReference;
        this.priceAmount = priceAmount != null ? priceAmount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "INR";
        this.enrolledAt = enrolledAt != null ? enrolledAt : Instant.now();
        this.confirmedAt = confirmedAt;
        this.activatedAt = activatedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.idempotencyKey = idempotencyKey;
    }

    public TrainingEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant createdAt, Instant updatedAt) {
        this(id, null, batchId, traineeId, status, paymentReference, BigDecimal.ZERO, "INR", createdAt, null, null, null, createdAt, updatedAt, traineeId, traineeId, null);
    }

    public TrainingEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant enrolledAt, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy, String idempotencyKey) {
        this(id, null, batchId, traineeId, status, paymentReference, BigDecimal.ZERO, "INR", enrolledAt, null, null, null, createdAt, updatedAt, createdBy, updatedBy, idempotencyKey);
    }

    public static TrainingEnrollment create(String batchId, String traineeId) {
        return create(batchId, traineeId, BigDecimal.ZERO, "INR", null, traineeId);
    }

    public static TrainingEnrollment create(String batchId, String traineeId, String idempotencyKey, String actor) {
        return create(batchId, traineeId, BigDecimal.ZERO, "INR", idempotencyKey, actor);
    }

    public static TrainingEnrollment create(String batchId, String traineeId, BigDecimal priceAmount, String currency, String idempotencyKey, String actor) {
        Instant now = Instant.now();
        String newId = UUID.randomUUID().toString();
        String code = generateEnrollmentCode(newId);
        return new TrainingEnrollment(newId, code, batchId, traineeId, EnrollmentStatus.PENDING, null,
                priceAmount, currency, now, null, null, null, now, now,
                actor != null ? actor : traineeId, actor != null ? actor : traineeId, idempotencyKey);
    }

    private static String generateEnrollmentCode(String id) {
        String clean = id != null ? id.replaceAll("-", "") : UUID.randomUUID().toString().replaceAll("-", "");
        String shortId = clean.length() >= 8 ? clean.substring(0, 8).toUpperCase() : (clean + "00000000").substring(0, 8).toUpperCase();
        return "ENR-2026-" + shortId;
    }

    public void markPaymentPending() {
        validateTransition(EnrollmentStatus.PAYMENT_PENDING);
        this.status = EnrollmentStatus.PAYMENT_PENDING;
        this.updatedAt = Instant.now();
    }

    public void markPaymentVerified(String paymentReference) {
        validateTransition(EnrollmentStatus.PAYMENT_VERIFIED);
        this.status = EnrollmentStatus.PAYMENT_VERIFIED;
        if (paymentReference != null) {
            this.paymentReference = paymentReference;
        }
        this.updatedAt = Instant.now();
    }

    public void confirm(String paymentReference) {
        validateTransition(EnrollmentStatus.CONFIRMED);
        this.status = EnrollmentStatus.CONFIRMED;
        if (paymentReference != null) {
            this.paymentReference = paymentReference;
        }
        if (this.confirmedAt == null) {
            this.confirmedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    public void activate() {
        validateTransition(EnrollmentStatus.ACTIVE);
        this.status = EnrollmentStatus.ACTIVE;
        if (this.activatedAt == null) {
            this.activatedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    public void complete() {
        validateTransition(EnrollmentStatus.COMPLETED);
        this.status = EnrollmentStatus.COMPLETED;
        if (this.completedAt == null) {
            this.completedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        validateTransition(EnrollmentStatus.CANCELLED);
        this.status = EnrollmentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void reject(String reason) {
        validateTransition(EnrollmentStatus.REJECTED);
        this.status = EnrollmentStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public void markPaymentFailed() {
        validateTransition(EnrollmentStatus.PAYMENT_FAILED);
        this.status = EnrollmentStatus.PAYMENT_FAILED;
        this.updatedAt = Instant.now();
    }

    public void waitlist() {
        validateTransition(EnrollmentStatus.WAITLISTED);
        this.status = EnrollmentStatus.WAITLISTED;
        this.updatedAt = Instant.now();
    }

    private void validateTransition(EnrollmentStatus targetStatus) {
        if (!this.status.isValidTransitionTo(targetStatus)) {
            throw new InvalidEnrollmentStateException("Illegal status transition from " + this.status + " to " + targetStatus + " for enrollment id=" + id);
        }
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
