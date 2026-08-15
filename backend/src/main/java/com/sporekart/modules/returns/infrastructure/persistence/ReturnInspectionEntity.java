package com.sporekart.modules.returns.infrastructure.persistence;

import com.sporekart.modules.returns.domain.InspectionOutcome;
import com.sporekart.modules.returns.domain.ReturnInspection;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_inspections")
public class ReturnInspectionEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnEntity returnEntity;

    @Column(name = "inspector_id", nullable = false, length = 128)
    private String inspectorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 32)
    private InspectionOutcome outcome;

    @Column(name = "notes", length = 512)
    private String notes;

    @Column(name = "inspected_at", nullable = false)
    private OffsetDateTime inspectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ReturnInspectionEntity() {}

    public static ReturnInspectionEntity fromDomain(ReturnInspection inspection, ReturnEntity parent) {
        ReturnInspectionEntity entity = new ReturnInspectionEntity();
        entity.id = inspection.id();
        entity.returnEntity = parent;
        entity.inspectorId = inspection.inspectorId();
        entity.outcome = inspection.outcome();
        entity.notes = inspection.notes();
        entity.inspectedAt = inspection.inspectedAt() != null ? inspection.inspectedAt() : OffsetDateTime.now();
        entity.createdAt = OffsetDateTime.now();
        return entity;
    }

    public void updateFromDomain(ReturnInspection inspection) {
        this.outcome = inspection.outcome();
        this.notes = inspection.notes();
        this.inspectedAt = inspection.inspectedAt();
    }

    public ReturnInspection toDomain() {
        return new ReturnInspection(
                id,
                returnEntity != null ? returnEntity.getId() : null,
                inspectorId,
                outcome,
                notes,
                inspectedAt
        );
    }
}
