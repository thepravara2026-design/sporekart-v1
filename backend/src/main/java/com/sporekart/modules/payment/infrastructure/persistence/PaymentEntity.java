package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private UUID id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 100)
    private String paymentReference;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private PaymentProviderType provider;

    @Column(name = "active_attempt_id")
    private UUID activeAttemptId;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAttemptEntity> attempts = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public PaymentEntity() {}

    public PaymentEntity(
            UUID id,
            String paymentReference,
            UUID orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            PaymentProviderType provider,
            UUID activeAttemptId,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.paymentReference = paymentReference;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.provider = provider;
        this.activeAttemptId = activeAttemptId;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentEntity fromDomain(Payment payment) {
        PaymentEntity entity = new PaymentEntity(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getActiveAttemptId(),
                (payment.getVersion() != null && payment.getVersion() > 0) ? payment.getVersion() : null,
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );

        if (payment.getAttempts() != null) {
            List<PaymentAttemptEntity> attemptEntities = payment.getAttempts().stream()
                    .map(att -> PaymentAttemptEntity.fromDomain(att, entity))
                    .toList();
            entity.setAttempts(attemptEntities);
        }

        return entity;
    }

    public Payment toDomain() {
        List<PaymentAttempt> domainAttempts = this.attempts != null
                ? this.attempts.stream().map(PaymentAttemptEntity::toDomain).toList()
                : new ArrayList<>();

        return new Payment(
                this.id,
                this.paymentReference,
                this.orderId,
                this.customerId,
                this.amount,
                this.currency,
                this.status,
                this.provider,
                this.activeAttemptId,
                domainAttempts,
                this.version,
                this.createdAt,
                this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public PaymentProviderType getProvider() { return provider; }
    public void setProvider(PaymentProviderType provider) { this.provider = provider; }

    public UUID getActiveAttemptId() { return activeAttemptId; }
    public void setActiveAttemptId(UUID activeAttemptId) { this.activeAttemptId = activeAttemptId; }

    public List<PaymentAttemptEntity> getAttempts() { return attempts; }
    public void setAttempts(List<PaymentAttemptEntity> attempts) {
        this.attempts.clear();
        if (attempts != null) {
            attempts.forEach(a -> a.setPayment(this));
            this.attempts.addAll(attempts);
        }
    }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentEntity that = (PaymentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
