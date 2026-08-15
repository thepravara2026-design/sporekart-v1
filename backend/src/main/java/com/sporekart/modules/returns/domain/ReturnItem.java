package com.sporekart.modules.returns.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class ReturnItem {
    private final UUID id;
    private final UUID returnId;
    private final UUID orderItemId;
    private final UUID productId;
    private final String sku;
    private final String productNameSnapshot;
    private final int requestedQuantity;
    private int approvedQuantity;
    private int receivedQuantity;
    private int acceptedQuantity;
    private int rejectedQuantity;
    private final BigDecimal unitPrice;
    private BigDecimal refundAmount;
    private final ReturnReasonCode reasonCode;

    public ReturnItem(
            UUID id,
            UUID returnId,
            UUID orderItemId,
            UUID productId,
            String sku,
            String productNameSnapshot,
            int requestedQuantity,
            int approvedQuantity,
            int receivedQuantity,
            int acceptedQuantity,
            int rejectedQuantity,
            BigDecimal unitPrice,
            BigDecimal refundAmount,
            ReturnReasonCode reasonCode
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.returnId = returnId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.sku = sku;
        this.productNameSnapshot = productNameSnapshot;
        this.requestedQuantity = requestedQuantity;
        this.approvedQuantity = approvedQuantity;
        this.receivedQuantity = receivedQuantity;
        this.acceptedQuantity = acceptedQuantity;
        this.rejectedQuantity = rejectedQuantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        this.reasonCode = reasonCode;
    }

    public static ReturnItem createNew(
            UUID returnId,
            UUID orderItemId,
            UUID productId,
            String sku,
            String productNameSnapshot,
            int requestedQuantity,
            BigDecimal unitPrice,
            ReturnReasonCode reasonCode
    ) {
        return new ReturnItem(
                UUID.randomUUID(),
                returnId,
                orderItemId,
                productId,
                sku,
                productNameSnapshot,
                requestedQuantity,
                0,
                0,
                0,
                0,
                unitPrice,
                BigDecimal.ZERO,
                reasonCode
        );
    }

    public UUID getId() { return id; }
    public UUID getReturnId() { return returnId; }
    public UUID getOrderItemId() { return orderItemId; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public int getApprovedQuantity() { return approvedQuantity; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public int getAcceptedQuantity() { return acceptedQuantity; }
    public int getRejectedQuantity() { return rejectedQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public ReturnReasonCode getReasonCode() { return reasonCode; }

    public void approve(int qty) {
        if (qty < 0 || qty > requestedQuantity) {
            throw new IllegalArgumentException("Approved quantity cannot exceed requested quantity");
        }
        this.approvedQuantity = qty;
    }

    public void recordReceipt(int qty) {
        if (qty < 0 || qty > approvedQuantity) {
            throw new IllegalArgumentException("Received quantity cannot exceed approved quantity");
        }
        this.receivedQuantity = qty;
    }

    public void recordInspection(int accepted, int rejected) {
        if (accepted < 0 || rejected < 0 || (accepted + rejected) > receivedQuantity) {
            throw new IllegalArgumentException("Accepted + rejected quantity cannot exceed received quantity");
        }
        this.acceptedQuantity = accepted;
        this.rejectedQuantity = rejected;
        this.refundAmount = unitPrice.multiply(BigDecimal.valueOf(accepted));
    }
}
