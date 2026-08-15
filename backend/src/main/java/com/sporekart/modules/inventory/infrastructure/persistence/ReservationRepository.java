package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockReservation;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {

    StockReservation save(StockReservation reservation);

    Optional<StockReservation> findById(UUID id);

    Optional<StockReservation> findByOrderId(UUID orderId);

    Optional<StockReservation> findByReservationReference(String reservationReference);

    List<StockReservation> findExpiredActiveReservations(OffsetDateTime now, Pageable pageable);
}
