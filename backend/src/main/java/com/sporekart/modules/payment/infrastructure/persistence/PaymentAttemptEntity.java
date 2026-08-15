package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttemptEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    @Column(name = "attempt_reference", nullable = false, unique = true, length = 100)
    private String attemptReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private PaymentProviderType provider;

    @Column(name = "provider_order_id", length = 100)
    private String providerOrderId;

    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @Column(name = "provider_signature", length = 255)
    private String providerSignature;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public PaymentAttemptEntity() {}

    public PaymentAttemptEntity(
            UUID id,
            PaymentEntity payment,
            String attemptReference,
            PaymentProviderType provider,
            String providerOrderId,
            String providerPaymentId,
            String providerSignature,
            PaymentStatus status,
            BigDecimal amount,
            String currency,
            String failureCode,
            String failureReason,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.payment = payment;
        this.attemptReference = attemptReference;
        this.provider = provider;
        this.providerOrderId = providerOrderId;
        this.providerPaymentId = providerPaymentId;
        this.providerSignature = providerSignature;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentAttemptEntity fromDomain(PaymentAttempt attempt, PaymentEntity paymentEntity) {
        return new PaymentAttemptEntity(
                attempt.getId(),
                paymentEntity,
                attempt.getAttemptReference(),
                attempt.getProvider(),
                attempt.getProviderOrderId(),
                attempt.getProviderPaymentId(),
                attempt.getProviderSignature(),
                attempt.getStatus(),
                attempt.getAmount(),
                attempt.getCurrency(),
                attempt.getFailureCode(),
                attempt.getFailureReason(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }

    public PaymentAttempt toDomain() {
        return new PaymentAttempt(
                this.id,
                this.payment != null ? this.payment.getId() : null,
                this.attemptReference,
                this.provider,
                this.providerOrderId,
                this.providerPaymentId,
                this.providerSignature,
                this.status,
                this.amount,
                this.currency,
                this.failureCode,
                this.failureReason,
                this.createdAt,
                this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PaymentEntity getPayment() { return payment; }
    public void setPayment(PaymentEntity payment) { this.payment = payment; }

    public String getAttemptReference() { return attemptReference; }
    public void setAttemptReference(String attemptReference) { this.attemptReference = attemptReference; }

    public PaymentProviderType getProvider() { return provider; }
    public void setProvider(PaymentProviderType provider) { this.provider = provider; }

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getProviderSignature() { return providerSignature; }
    public void setProviderSignature(String providerSignature) { this.providerSignature = providerSignature; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentAttemptEntity that = (PaymentAttemptEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
