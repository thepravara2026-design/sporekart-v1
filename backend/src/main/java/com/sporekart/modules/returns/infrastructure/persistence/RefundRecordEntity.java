package com.sporekart.modules.returns.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refund_records")
public class RefundRecordEntity {

    @Id
    private UUID id;

    @Column(name = "refund_reference", nullable = false, unique = true, length = 64)
    private String refundReference;

    @Column(name = "return_id", nullable = false)
    private UUID returnId;

    @Column(name = "return_reference", nullable = false, length = 64)
    private String returnReference;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "payment_reference", nullable = false, length = 64)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 32)
    private PaymentProviderType provider;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "provider_refund_id", length = 128)
    private String providerRefundId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public RefundRecordEntity() {}

    public RefundRecordEntity(
            UUID id,
            String refundReference,
            UUID returnId,
            String returnReference,
            UUID orderId,
            String customerId,
            UUID paymentId,
            String paymentReference,
            PaymentProviderType provider,
            BigDecimal amount,
            String currency,
            String status,
            String failureReason,
            String providerRefundId,
            String idempotencyKey,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.refundReference = refundReference;
        this.returnId = returnId;
        this.returnReference = returnReference;
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.paymentReference = paymentReference;
        this.provider = provider;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status;
        this.failureReason = failureReason;
        this.providerRefundId = providerRefundId;
        this.idempotencyKey = idempotencyKey;
        this.version = (version != null && version <= 0) ? null : version;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getRefundReference() { return refundReference; }
    public UUID getReturnId() { return returnId; }
    public String getReturnReference() { return returnReference; }
    public UUID getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public UUID getPaymentId() { return paymentId; }
    public String getPaymentReference() { return paymentReference; }
    public PaymentProviderType getProvider() { return provider; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public String getProviderRefundId() { return providerRefundId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void updateStatus(String newStatus, String providerRefundId, String failureReason) {
        this.status = newStatus;
        if (providerRefundId != null) this.providerRefundId = providerRefundId;
        if (failureReason != null) this.failureReason = failureReason;
        this.updatedAt = OffsetDateTime.now();
    }
}
