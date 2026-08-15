package com.sporekart.modules.payment.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentAttempt {

    private final UUID id;
    private final UUID paymentId;
    private final String attemptReference;
    private final PaymentProviderType provider;
    private String providerOrderId;
    private String providerPaymentId;
    private String providerSignature;
    private PaymentStatus status;
    private final BigDecimal amount;
    private final String currency;
    private String failureCode;
    private String failureReason;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public PaymentAttempt(
            UUID id,
            UUID paymentId,
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
        this.id = Objects.requireNonNull(id, "Attempt ID cannot be null");
        this.paymentId = Objects.requireNonNull(paymentId, "Payment ID cannot be null");
        this.attemptReference = Objects.requireNonNull(attemptReference, "Attempt reference cannot be null");
        this.provider = Objects.requireNonNull(provider, "Payment provider cannot be null");
        this.providerOrderId = providerOrderId;
        this.providerPaymentId = providerPaymentId;
        this.providerSignature = providerSignature;
        this.status = Objects.requireNonNull(status, "Payment status cannot be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public static PaymentAttempt createNewAttempt(
            UUID paymentId,
            String attemptReference,
            PaymentProviderType provider,
            BigDecimal amount,
            String currency
    ) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new PaymentAttempt(id, paymentId, attemptReference, provider, null, null, null, PaymentStatus.CREATED, amount, currency, null, null, now, now);
    }

    public void updateProviderDetails(String providerOrderId, String providerPaymentId, String providerSignature) {
        if (providerOrderId != null) this.providerOrderId = providerOrderId;
        if (providerPaymentId != null) this.providerPaymentId = providerPaymentId;
        if (providerSignature != null) this.providerSignature = providerSignature;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markSuccess(String providerPaymentId, String providerSignature) {
        if (providerPaymentId != null) this.providerPaymentId = providerPaymentId;
        if (providerSignature != null) this.providerSignature = providerSignature;
        this.status = PaymentStatus.SUCCESS;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed(String failureCode, String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markPending() {
        this.status = PaymentStatus.PENDING;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public String getAttemptReference() { return attemptReference; }
    public PaymentProviderType getProvider() { return provider; }
    public String getProviderOrderId() { return providerOrderId; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public String getProviderSignature() { return providerSignature; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getFailureCode() { return failureCode; }
    public String getFailureReason() { return failureReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentAttempt that = (PaymentAttempt) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
