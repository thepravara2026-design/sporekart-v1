package com.sporekart.modules.returns.infrastructure.persistence;

import com.sporekart.modules.returns.domain.ReturnItem;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_items")
public class ReturnItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnEntity returnEntity;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "requested_quantity", nullable = false)
    private int requestedQuantity;

    @Column(name = "approved_quantity", nullable = false)
    private int approvedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    @Column(name = "accepted_quantity", nullable = false)
    private int acceptedQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    private int rejectedQuantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 32)
    private ReturnReasonCode reasonCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ReturnItemEntity() {}

    public static ReturnItemEntity fromDomain(ReturnItem item, ReturnEntity parent) {
        ReturnItemEntity entity = new ReturnItemEntity();
        entity.id = item.getId();
        entity.returnEntity = parent;
        entity.orderItemId = item.getOrderItemId();
        entity.productId = item.getProductId();
        entity.sku = item.getSku();
        entity.productNameSnapshot = item.getProductNameSnapshot();
        entity.requestedQuantity = item.getRequestedQuantity();
        entity.approvedQuantity = item.getApprovedQuantity();
        entity.receivedQuantity = item.getReceivedQuantity();
        entity.acceptedQuantity = item.getAcceptedQuantity();
        entity.rejectedQuantity = item.getRejectedQuantity();
        entity.unitPrice = item.getUnitPrice();
        entity.refundAmount = item.getRefundAmount();
        entity.reasonCode = item.getReasonCode();
        entity.createdAt = OffsetDateTime.now();
        entity.updatedAt = OffsetDateTime.now();
        return entity;
    }

    public void updateFromDomain(ReturnItem item) {
        this.approvedQuantity = item.getApprovedQuantity();
        this.receivedQuantity = item.getReceivedQuantity();
        this.acceptedQuantity = item.getAcceptedQuantity();
        this.rejectedQuantity = item.getRejectedQuantity();
        this.refundAmount = item.getRefundAmount();
        this.updatedAt = OffsetDateTime.now();
    }

    public ReturnItem toDomain() {
        return new ReturnItem(
                id,
                returnEntity != null ? returnEntity.getId() : null,
                orderItemId,
                productId,
                sku,
                productNameSnapshot,
                requestedQuantity,
                approvedQuantity,
                receivedQuantity,
                acceptedQuantity,
                rejectedQuantity,
                unitPrice,
                refundAmount,
                reasonCode
        );
    }

    public UUID getId() { return id; }
}
