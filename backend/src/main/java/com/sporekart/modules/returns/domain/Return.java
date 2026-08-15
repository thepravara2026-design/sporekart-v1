package com.sporekart.modules.returns.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Return {
    private final UUID id;
    private final String returnReference;
    private final UUID orderId;
    private final String orderReference;
    private final String customerId;
    private ReturnStatus status;
    private final ReturnReasonCode reasonCode;
    private final String reasonDescription;
    private final String evidenceUrls;
    private final String policyVersion;
    private final OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private OffsetDateTime receivedAt;
    private OffsetDateTime inspectedAt;
    private OffsetDateTime completedAt;
    private UUID reverseShipmentId;
    private long version;

    private final List<ReturnItem> items = new ArrayList<>();
    private final List<ReturnStatusHistory> statusHistory = new ArrayList<>();
    private ReturnInspection inspection;

    public Return(
            UUID id,
            String returnReference,
            UUID orderId,
            String orderReference,
            String customerId,
            ReturnStatus status,
            ReturnReasonCode reasonCode,
            String reasonDescription,
            String evidenceUrls,
            String policyVersion,
            OffsetDateTime requestedAt,
            OffsetDateTime approvedAt,
            OffsetDateTime receivedAt,
            OffsetDateTime inspectedAt,
            OffsetDateTime completedAt,
            UUID reverseShipmentId,
            long version,
            List<ReturnItem> items,
            List<ReturnStatusHistory> statusHistory,
            ReturnInspection inspection
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.returnReference = returnReference;
        this.orderId = orderId;
        this.orderReference = orderReference;
        this.customerId = customerId;
        this.status = status != null ? status : ReturnStatus.REQUESTED;
        this.reasonCode = reasonCode;
        this.reasonDescription = reasonDescription;
        this.evidenceUrls = evidenceUrls;
        this.policyVersion = policyVersion != null ? policyVersion : "v1.0";
        this.requestedAt = requestedAt != null ? requestedAt : OffsetDateTime.now();
        this.approvedAt = approvedAt;
        this.receivedAt = receivedAt;
        this.inspectedAt = inspectedAt;
        this.completedAt = completedAt;
        this.reverseShipmentId = reverseShipmentId;
        this.version = version;
        if (items != null) this.items.addAll(items);
        if (statusHistory != null) this.statusHistory.addAll(statusHistory);
        this.inspection = inspection;
    }

    public Return(
            UUID id,
            String returnReference,
            UUID orderId,
            String orderReference,
            String customerId,
            ReturnStatus status,
            ReturnReasonCode reasonCode,
            String reasonDescription,
            String evidenceUrls,
            String policyVersion,
            OffsetDateTime requestedAt,
            OffsetDateTime approvedAt,
            OffsetDateTime receivedAt,
            OffsetDateTime inspectedAt,
            OffsetDateTime completedAt,
            long version,
            List<ReturnItem> items,
            List<ReturnStatusHistory> statusHistory,
            ReturnInspection inspection
    ) {
        this(id, returnReference, orderId, orderReference, customerId, status, reasonCode, reasonDescription, evidenceUrls, policyVersion, requestedAt, approvedAt, receivedAt, inspectedAt, completedAt, null, version, items, statusHistory, inspection);
    }

    public static Return createNewRequest(
            String returnReference,
            UUID orderId,
            String orderReference,
            String customerId,
            ReturnReasonCode reasonCode,
            String reasonDescription,
            String evidenceUrls,
            String policyVersion
    ) {
        UUID returnId = UUID.randomUUID();
        Return ret = new Return(
                returnId,
                returnReference,
                orderId,
                orderReference,
                customerId,
                ReturnStatus.REQUESTED,
                reasonCode,
                reasonDescription,
                evidenceUrls,
                policyVersion,
                OffsetDateTime.now(),
                null, null, null, null,
                0L,
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );
        ret.recordStatusHistory(null, ReturnStatus.REQUESTED, "Return request created", "CUSTOMER", customerId, null);
        return ret;
    }

    public UUID getId() { return id; }
    public String getReturnReference() { return returnReference; }
    public UUID getOrderId() { return orderId; }
    public String getOrderReference() { return orderReference; }
    public String getCustomerId() { return customerId; }
    public ReturnStatus getStatus() { return status; }
    public ReturnReasonCode getReasonCode() { return reasonCode; }
    public String getReasonDescription() { return reasonDescription; }
    public String getEvidenceUrls() { return evidenceUrls; }
    public String getPolicyVersion() { return policyVersion; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public OffsetDateTime getInspectedAt() { return inspectedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public UUID getReverseShipmentId() { return reverseShipmentId; }
    public long getVersion() { return version; }
    public List<ReturnItem> getItems() { return Collections.unmodifiableList(items); }
    public List<ReturnStatusHistory> getStatusHistory() { return Collections.unmodifiableList(statusHistory); }
    public ReturnInspection getInspection() { return inspection; }

    public void addItem(ReturnItem item) {
        this.items.add(item);
    }

    public void transitionTo(ReturnStatus targetStatus, String reason, String actorType, String actorId, String correlationId) {
        ReturnStateMachine.validateTransition(this.status, targetStatus);
        ReturnStatus previous = this.status;
        this.status = targetStatus;
        recordStatusHistory(previous, targetStatus, reason, actorType, actorId, correlationId);
    }

    public void approve(String adminId, String notes, String correlationId) {
        transitionTo(ReturnStatus.APPROVED, notes != null ? notes : "Return approved by admin", "ADMIN", adminId, correlationId);
        this.approvedAt = OffsetDateTime.now();
        for (ReturnItem item : items) {
            item.approve(item.getRequestedQuantity());
        }
    }

    public void assignReverseShipment(UUID reverseShipmentId, String actorId, String correlationId) {
        this.reverseShipmentId = reverseShipmentId;
        transitionTo(ReturnStatus.REVERSE_SHIPMENT_CREATED, "Reverse shipment created", "ADMIN", actorId, correlationId);
    }

    public void markPickupFailed(String reason, String actorId, String correlationId) {
        transitionTo(ReturnStatus.PICKUP_FAILED, reason != null ? reason : "Return pickup failed", "SYSTEM", actorId, correlationId);
    }

    public void reject(String adminId, String reason, String correlationId) {
        transitionTo(ReturnStatus.REJECTED, reason, "ADMIN", adminId, correlationId);
        this.completedAt = OffsetDateTime.now();
    }

    public void cancel(String customerId, String reason, String correlationId) {
        if (!this.customerId.equals(customerId)) {
            throw new ReturnAccessDeniedException("Customer does not own this return record");
        }
        transitionTo(ReturnStatus.CANCELLED, reason != null ? reason : "Cancelled by customer", "CUSTOMER", customerId, correlationId);
        this.completedAt = OffsetDateTime.now();
    }

    public void markReceived(String correlationId) {
        transitionTo(ReturnStatus.RECEIVED, "Reverse shipment received at warehouse", "SYSTEM", "SYSTEM", correlationId);
        this.receivedAt = OffsetDateTime.now();
        for (ReturnItem item : items) {
            item.recordReceipt(item.getApprovedQuantity());
        }
        transitionTo(ReturnStatus.INSPECTION_PENDING, "Awaiting warehouse inspection", "SYSTEM", "SYSTEM", correlationId);
    }

    public void recordInspectionOutcome(String inspectorId, InspectionOutcome outcome, String notes, List<ReturnItemInspectionRecord> itemInspections, String correlationId) {
        transitionTo(ReturnStatus.INSPECTED, "Warehouse inspection completed", "ADMIN", inspectorId, correlationId);
        this.inspection = ReturnInspection.create(this.id, inspectorId, outcome, notes);
        this.inspectedAt = OffsetDateTime.now();

        if (itemInspections != null && !itemInspections.isEmpty()) {
            for (ReturnItemInspectionRecord rec : itemInspections) {
                items.stream()
                        .filter(i -> i.getId().equals(rec.returnItemId()))
                        .findFirst()
                        .ifPresent(i -> i.recordInspection(rec.acceptedQuantity(), rec.rejectedQuantity()));
            }
        } else {
            // Default full acceptance or rejection if item-level details omitted
            for (ReturnItem item : items) {
                if (outcome == InspectionOutcome.ACCEPTED) {
                    item.recordInspection(item.getReceivedQuantity(), 0);
                } else if (outcome == InspectionOutcome.RETURN_REJECTED) {
                    item.recordInspection(0, item.getReceivedQuantity());
                } else if (outcome == InspectionOutcome.PARTIALLY_ACCEPTED) {
                    int accepted = item.getReceivedQuantity() / 2;
                    item.recordInspection(accepted, item.getReceivedQuantity() - accepted);
                }
            }
        }

        if (outcome == InspectionOutcome.ACCEPTED) {
            transitionTo(ReturnStatus.ACCEPTED, "Return accepted after inspection", "ADMIN", inspectorId, correlationId);
            transitionTo(ReturnStatus.REFUND_PENDING, "Awaiting refund orchestration", "SYSTEM", "SYSTEM", correlationId);
        } else if (outcome == InspectionOutcome.PARTIALLY_ACCEPTED) {
            transitionTo(ReturnStatus.PARTIALLY_ACCEPTED, "Return partially accepted after inspection", "ADMIN", inspectorId, correlationId);
            transitionTo(ReturnStatus.REFUND_PENDING, "Awaiting partial refund orchestration", "SYSTEM", "SYSTEM", correlationId);
        } else {
            transitionTo(ReturnStatus.RETURN_REJECTED, "Return rejected after inspection", "ADMIN", inspectorId, correlationId);
            this.completedAt = OffsetDateTime.now();
        }
    }

    public void markRefunded(String correlationId) {
        transitionTo(ReturnStatus.REFUNDED, "Refund successfully processed", "SYSTEM", "SYSTEM", correlationId);
        this.completedAt = OffsetDateTime.now();
    }

    public BigDecimal calculateTotalRefundableAmount() {
        BigDecimal sum = items.stream()
                .map(ReturnItem::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.ZERO) == 0 && !items.isEmpty()) {
            return items.stream()
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getRequestedQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return sum;
    }

    private void recordStatusHistory(ReturnStatus previous, ReturnStatus target, String reason, String actorType, String actorId, String correlationId) {
        this.statusHistory.add(ReturnStatusHistory.recordTransition(this.id, previous, target, reason, actorType, actorId, correlationId));
    }

    public record ReturnItemInspectionRecord(UUID returnItemId, int acceptedQuantity, int rejectedQuantity) {}
}
