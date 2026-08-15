package com.sporekart.modules.inventory.domain;

import com.sporekart.modules.inventory.domain.exception.ReservationAlreadyReleasedException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StockReservation {

    private final UUID id;
    private final String reservationReference;
    private final UUID orderId;
    private ReservationStatus status;
    private final OffsetDateTime expiresAt;
    private String releaseReason;
    private final List<StockReservationItem> items;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public StockReservation(
            UUID id,
            String reservationReference,
            UUID orderId,
            ReservationStatus status,
            OffsetDateTime expiresAt,
            String releaseReason,
            List<StockReservationItem> items,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Reservation ID cannot be null");
        this.reservationReference = Objects.requireNonNull(reservationReference, "Reservation reference cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.status = Objects.requireNonNull(status, "Reservation status cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration timestamp cannot be null");
        this.releaseReason = releaseReason;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public static StockReservation createActiveReservation(
            String reservationReference,
            UUID orderId,
            OffsetDateTime expiresAt,
            List<StockReservationItem> items
    ) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new StockReservation(id, reservationReference, orderId, ReservationStatus.ACTIVE, expiresAt, null, items, now, now);
    }

    public void release(String reason) {
        if (this.status == ReservationStatus.RELEASED) {
            // Idempotent release
            return;
        }
        if (this.status != ReservationStatus.ACTIVE) {
            throw new ReservationAlreadyReleasedException(this.id, this.status);
        }
        this.status = ReservationStatus.RELEASED;
        this.releaseReason = reason != null ? reason : "CUSTOMER_RELEASE";
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean expire() {
        if (this.status == ReservationStatus.ACTIVE) {
            this.status = ReservationStatus.EXPIRED;
            this.releaseReason = "SYSTEM_EXPIRY";
            this.updatedAt = OffsetDateTime.now();
            return true;
        }
        return false;
    }

    public void confirm() {
        if (this.status == ReservationStatus.ACTIVE) {
            this.status = ReservationStatus.CONFIRMED;
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public void commit() {
        if (this.status == ReservationStatus.COMMITTED) {
            // Idempotent commit
            return;
        }
        if (this.status == ReservationStatus.ACTIVE || this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.COMMITTED;
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public boolean isActive() {
        return this.status == ReservationStatus.ACTIVE;
    }

    public boolean isExpired(OffsetDateTime now) {
        return this.status == ReservationStatus.ACTIVE && now.isAfter(this.expiresAt);
    }

    // Getters
    public UUID getId() { return id; }
    public String getReservationReference() { return reservationReference; }
    public UUID getOrderId() { return orderId; }
    public ReservationStatus getStatus() { return status; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public String getReleaseReason() { return releaseReason; }
    public List<StockReservationItem> getItems() { return Collections.unmodifiableList(items); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockReservation that = (StockReservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
