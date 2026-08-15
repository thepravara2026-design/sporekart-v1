package com.sporekart.modules.returns.infrastructure.persistence;

import com.sporekart.modules.returns.domain.ReturnStatus;
import com.sporekart.modules.returns.domain.ReturnStatusHistory;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_status_history")
public class ReturnStatusHistoryEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnEntity returnEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private ReturnStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 32)
    private ReturnStatus newStatus;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "actor_type", nullable = false, length = 32)
    private String actorType;

    @Column(name = "actor_id", length = 128)
    private String actorId;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ReturnStatusHistoryEntity() {}

    public static ReturnStatusHistoryEntity fromDomain(ReturnStatusHistory h, ReturnEntity parent) {
        ReturnStatusHistoryEntity entity = new ReturnStatusHistoryEntity();
        entity.id = h.id();
        entity.returnEntity = parent;
        entity.previousStatus = h.previousStatus();
        entity.newStatus = h.newStatus();
        entity.reason = h.reason();
        entity.actorType = h.actorType();
        entity.actorId = h.actorId();
        entity.correlationId = h.correlationId();
        entity.createdAt = h.createdAt() != null ? h.createdAt() : OffsetDateTime.now();
        return entity;
    }

    public ReturnStatusHistory toDomain() {
        return new ReturnStatusHistory(
                id,
                returnEntity != null ? returnEntity.getId() : null,
                previousStatus,
                newStatus,
                reason,
                actorType,
                actorId,
                correlationId,
                createdAt
        );
    }

    public UUID getId() { return id; }
}
