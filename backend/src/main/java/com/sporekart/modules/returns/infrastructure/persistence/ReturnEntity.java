package com.sporekart.modules.returns.infrastructure.persistence;

import com.sporekart.modules.returns.domain.InspectionOutcome;
import com.sporekart.modules.returns.domain.Return;
import com.sporekart.modules.returns.domain.ReturnInspection;
import com.sporekart.modules.returns.domain.ReturnItem;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import com.sporekart.modules.returns.domain.ReturnStatus;
import com.sporekart.modules.returns.domain.ReturnStatusHistory;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "returns")
public class ReturnEntity {

    @Id
    private UUID id;

    @Column(name = "return_reference", nullable = false, unique = true, length = 64)
    private String returnReference;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "order_reference", nullable = false, length = 64)
    private String orderReference;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReturnStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 32)
    private ReturnReasonCode reasonCode;

    @Column(name = "reason_description", length = 512)
    private String reasonDescription;

    @Column(name = "evidence_urls", columnDefinition = "TEXT")
    private String evidenceUrls;

    @Column(name = "policy_version", nullable = false, length = 32)
    private String policyVersion;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "inspected_at")
    private OffsetDateTime inspectedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "reverse_shipment_id")
    private UUID reverseShipmentId;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReturnItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReturnStatusHistoryEntity> statusHistory = new ArrayList<>();

    @OneToOne(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ReturnInspectionEntity inspection;

    public ReturnEntity() {}

    public static ReturnEntity fromDomain(Return r) {
        ReturnEntity entity = new ReturnEntity();
        entity.id = r.getId();
        entity.returnReference = r.getReturnReference();
        entity.orderId = r.getOrderId();
        entity.orderReference = r.getOrderReference();
        entity.customerId = r.getCustomerId();
        entity.status = r.getStatus();
        entity.reasonCode = r.getReasonCode();
        entity.reasonDescription = r.getReasonDescription();
        entity.evidenceUrls = r.getEvidenceUrls();
        entity.policyVersion = r.getPolicyVersion();
        entity.requestedAt = r.getRequestedAt();
        entity.approvedAt = r.getApprovedAt();
        entity.receivedAt = r.getReceivedAt();
        entity.inspectedAt = r.getInspectedAt();
        entity.completedAt = r.getCompletedAt();
        entity.reverseShipmentId = r.getReverseShipmentId();
        entity.version = (r.getVersion() <= 0) ? null : r.getVersion();
        entity.createdAt = r.getRequestedAt() != null ? r.getRequestedAt() : OffsetDateTime.now();
        entity.updatedAt = OffsetDateTime.now();

        if (r.getItems() != null) {
            entity.items = r.getItems().stream()
                    .map(item -> ReturnItemEntity.fromDomain(item, entity))
                    .collect(Collectors.toList());
        }

        if (r.getStatusHistory() != null) {
            entity.statusHistory = r.getStatusHistory().stream()
                    .map(sh -> ReturnStatusHistoryEntity.fromDomain(sh, entity))
                    .collect(Collectors.toList());
        }

        if (r.getInspection() != null) {
            entity.inspection = ReturnInspectionEntity.fromDomain(r.getInspection(), entity);
        }

        return entity;
    }

    public void updateFromDomain(Return r) {
        this.status = r.getStatus();
        this.approvedAt = r.getApprovedAt();
        this.receivedAt = r.getReceivedAt();
        this.inspectedAt = r.getInspectedAt();
        this.completedAt = r.getCompletedAt();
        this.reverseShipmentId = r.getReverseShipmentId();
        this.updatedAt = OffsetDateTime.now();

        // Update items
        for (ReturnItem domainItem : r.getItems()) {
            ReturnItemEntity itemEntity = this.items.stream()
                    .filter(ie -> ie.getId().equals(domainItem.getId()))
                    .findFirst()
                    .orElse(null);
            if (itemEntity != null) {
                itemEntity.updateFromDomain(domainItem);
            } else {
                this.items.add(ReturnItemEntity.fromDomain(domainItem, this));
            }
        }

        // Append new status history records
        for (ReturnStatusHistory domainHist : r.getStatusHistory()) {
            boolean exists = this.statusHistory.stream()
                    .anyMatch(he -> he.getId().equals(domainHist.id()));
            if (!exists) {
                this.statusHistory.add(ReturnStatusHistoryEntity.fromDomain(domainHist, this));
            }
        }

        if (r.getInspection() != null) {
            if (this.inspection == null) {
                this.inspection = ReturnInspectionEntity.fromDomain(r.getInspection(), this);
            } else {
                this.inspection.updateFromDomain(r.getInspection());
            }
        }
    }

    public Return toDomain() {
        List<ReturnItem> domainItems = items.stream()
                .map(ReturnItemEntity::toDomain)
                .collect(Collectors.toList());

        List<ReturnStatusHistory> domainHistory = statusHistory.stream()
                .map(ReturnStatusHistoryEntity::toDomain)
                .collect(Collectors.toList());

        ReturnInspection domainInspection = inspection != null ? inspection.toDomain() : null;

        return new Return(
                id,
                returnReference,
                orderId,
                orderReference,
                customerId,
                status,
                reasonCode,
                reasonDescription,
                evidenceUrls,
                policyVersion,
                requestedAt,
                approvedAt,
                receivedAt,
                inspectedAt,
                completedAt,
                reverseShipmentId,
                version != null ? version : 0L,
                domainItems,
                domainHistory,
                domainInspection
        );
    }

    public UUID getId() { return id; }
    public String getReturnReference() { return returnReference; }
    public UUID getOrderId() { return orderId; }
    public String getOrderReference() { return orderReference; }
    public String getCustomerId() { return customerId; }
    public ReturnStatus getStatus() { return status; }
    public ReturnReasonCode getReasonCode() { return reasonCode; }
    public Long getVersion() { return version; }
}
