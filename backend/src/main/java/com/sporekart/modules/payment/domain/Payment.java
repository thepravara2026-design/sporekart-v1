package com.sporekart.modules.payment.domain;

import com.sporekart.modules.payment.domain.exception.PaymentInvalidStateException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final String paymentReference;
    private final UUID orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private final PaymentProviderType provider;
    private UUID activeAttemptId;
    private final List<PaymentAttempt> attempts;
    private Long version;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Payment(
            UUID id,
            String paymentReference,
            UUID orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            PaymentProviderType provider,
            UUID activeAttemptId,
            List<PaymentAttempt> attempts,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Payment ID cannot be null");
        this.paymentReference = Objects.requireNonNull(paymentReference, "Payment reference cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = Objects.requireNonNull(status, "Payment status cannot be null");
        this.provider = Objects.requireNonNull(provider, "Payment provider cannot be null");
        this.activeAttemptId = activeAttemptId;
        this.attempts = attempts != null ? new ArrayList<>(attempts) : new ArrayList<>();
        this.version = version != null ? version : 0L;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public static Payment createNewPayment(
            String paymentReference,
            UUID orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentProviderType provider
    ) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new Payment(id, paymentReference, orderId, customerId, amount, currency, PaymentStatus.CREATED, provider, null, List.of(), 0L, now, now);
    }

    public PaymentAttempt createAttempt(String attemptReference) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new PaymentInvalidStateException("Cannot create new payment attempt for an already successful payment");
        }
        PaymentAttempt attempt = PaymentAttempt.createNewAttempt(this.id, attemptReference, this.provider, this.amount, this.currency);
        this.attempts.add(attempt);
        this.activeAttemptId = attempt.getId();
        this.status = PaymentStatus.PENDING;
        this.updatedAt = OffsetDateTime.now();
        return attempt;
    }

    public void markSuccess(UUID attemptId, String providerPaymentId, String providerSignature) {
        if (this.status == PaymentStatus.SUCCESS) {
            // Idempotent success replay
            return;
        }
        PaymentAttempt attempt = findAttempt(attemptId);
        if (attempt != null) {
            attempt.markSuccess(providerPaymentId, providerSignature);
        }
        this.status = PaymentStatus.SUCCESS;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed(UUID attemptId, String failureCode, String failureReason) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new PaymentInvalidStateException("Cannot mark a SUCCESS payment as FAILED");
        }
        PaymentAttempt attempt = findAttempt(attemptId);
        if (attempt != null) {
            attempt.markFailed(failureCode, failureReason);
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markCancelled(String reason) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new PaymentInvalidStateException("Cannot cancel an already successful payment");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markExpired() {
        if (this.status == PaymentStatus.SUCCESS) {
            return;
        }
        this.status = PaymentStatus.EXPIRED;
        this.updatedAt = OffsetDateTime.now();
    }

    private PaymentAttempt findAttempt(UUID attemptId) {
        if (attemptId == null) return null;
        return this.attempts.stream()
                .filter(a -> a.getId().equals(attemptId))
                .findFirst()
                .orElse(null);
    }

    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCESS;
    }

    // Getters
    public UUID getId() { return id; }
    public String getPaymentReference() { return paymentReference; }
    public UUID getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public PaymentProviderType getProvider() { return provider; }
    public UUID getActiveAttemptId() { return activeAttemptId; }
    public List<PaymentAttempt> getAttempts() { return Collections.unmodifiableList(attempts); }
    public Long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
