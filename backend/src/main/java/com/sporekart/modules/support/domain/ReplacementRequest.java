package com.sporekart.modules.support.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "replacement_requests")
public class ReplacementRequest {

    @Id
    private String id;

    @Column(name = "replacement_reference", nullable = false, unique = true, length = 32)
    private String replacementReference;

    @Column(name = "ticket_id", nullable = false, length = 36)
    private String ticketId;

    @Column(name = "ticket_number", nullable = false, length = 32)
    private String ticketNumber;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "order_reference", nullable = false, length = 64)
    private String orderReference;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "order_item_id", nullable = false, length = 36)
    private String orderItemId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReplacementStatus status;

    @Column(name = "reservation_id", length = 36)
    private String reservationId;

    @Column(name = "replacement_shipment_id", length = 36)
    private String replacementShipmentId;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ReplacementRequest() {}

    public static ReplacementRequest create(
            String replacementReference,
            String ticketId,
            String ticketNumber,
            String orderId,
            String orderReference,
            String customerId,
            String orderItemId,
            String productId,
            String sku,
            int quantity,
            String reason
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        ReplacementRequest req = new ReplacementRequest();
        req.id = UUID.randomUUID().toString();
        req.replacementReference = replacementReference;
        req.ticketId = ticketId;
        req.ticketNumber = ticketNumber;
        req.orderId = orderId;
        req.orderReference = orderReference;
        req.customerId = customerId;
        req.orderItemId = orderItemId;
        req.productId = productId;
        req.sku = sku;
        req.quantity = quantity;
        req.reason = reason;
        req.status = ReplacementStatus.REQUESTED;
        req.createdAt = now;
        req.updatedAt = now;
        return req;
    }

    public void markInventoryReserved(String reservationId) {
        this.reservationId = reservationId;
        this.status = ReplacementStatus.INVENTORY_RESERVED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void approve(String adminNotes) {
        this.adminNotes = adminNotes;
        this.status = ReplacementStatus.APPROVED;
        this.approvedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void markShipmentCreated(String shipmentId) {
        this.replacementShipmentId = shipmentId;
        this.status = ReplacementStatus.SHIPMENT_CREATED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void reject(String reason) {
        this.adminNotes = reason;
        this.status = ReplacementStatus.REJECTED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed(String failureReason) {
        this.adminNotes = failureReason;
        this.status = ReplacementStatus.FAILED;
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public String getReplacementReference() { return replacementReference; }
    public String getTicketId() { return ticketId; }
    public String getTicketNumber() { return ticketNumber; }
    public String getOrderId() { return orderId; }
    public String getOrderReference() { return orderReference; }
    public String getCustomerId() { return customerId; }
    public String getOrderItemId() { return orderItemId; }
    public String getProductId() { return productId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public String getReason() { return reason; }
    public ReplacementStatus getStatus() { return status; }
    public String getReservationId() { return reservationId; }
    public String getReplacementShipmentId() { return replacementShipmentId; }
    public String getAdminNotes() { return adminNotes; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
