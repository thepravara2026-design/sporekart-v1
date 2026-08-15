package com.sporekart.modules.inventory.application.dto;

import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockReservation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReservationDto(
        UUID id,
        String reservationReference,
        UUID orderId,
        ReservationStatus status,
        OffsetDateTime expiresAt,
        String releaseReason,
        List<ReservationItemDto> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ReservationDto fromDomain(StockReservation reservation) {
        List<ReservationItemDto> itemDtos = reservation.getItems() != null
                ? reservation.getItems().stream().map(ReservationItemDto::fromDomain).toList()
                : List.of();

        return new ReservationDto(
                reservation.getId(),
                reservation.getReservationReference(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getReleaseReason(),
                itemDtos,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
