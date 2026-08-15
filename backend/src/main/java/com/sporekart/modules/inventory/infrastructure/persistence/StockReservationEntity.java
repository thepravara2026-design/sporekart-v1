package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.domain.StockReservationItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
public class StockReservationEntity {

    @Id
    private UUID id;

    @Column(name = "reservation_reference", nullable = false, unique = true, length = 100)
    private String reservationReference;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "release_reason", length = 100)
    private String releaseReason;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockReservationItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public StockReservationEntity() {}

    public StockReservationEntity(
            UUID id,
            String reservationReference,
            UUID orderId,
            ReservationStatus status,
            OffsetDateTime expiresAt,
            String releaseReason,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.reservationReference = reservationReference;
        this.orderId = orderId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.releaseReason = releaseReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StockReservationEntity fromDomain(StockReservation reservation) {
        StockReservationEntity entity = new StockReservationEntity(
                reservation.getId(),
                reservation.getReservationReference(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getReleaseReason(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );

        if (reservation.getItems() != null) {
            List<StockReservationItemEntity> itemEntities = reservation.getItems().stream()
                    .map(item -> StockReservationItemEntity.fromDomain(item, entity))
                    .toList();
            entity.setItems(itemEntities);
        }

        return entity;
    }

    public StockReservation toDomain() {
        List<StockReservationItem> domainItems = this.items != null
                ? this.items.stream().map(StockReservationItemEntity::toDomain).toList()
                : new ArrayList<>();

        return new StockReservation(
                this.id,
                this.reservationReference,
                this.orderId,
                this.status,
                this.expiresAt,
                this.releaseReason,
                domainItems,
                this.createdAt,
                this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReservationReference() { return reservationReference; }
    public void setReservationReference(String reservationReference) { this.reservationReference = reservationReference; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getReleaseReason() { return releaseReason; }
    public void setReleaseReason(String releaseReason) { this.releaseReason = releaseReason; }

    public List<StockReservationItemEntity> getItems() { return items; }
    public void setItems(List<StockReservationItemEntity> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(item -> item.setReservation(this));
            this.items.addAll(items);
        }
    }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockReservationEntity that = (StockReservationEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
