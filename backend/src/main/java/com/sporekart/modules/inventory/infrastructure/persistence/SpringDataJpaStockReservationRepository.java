package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaStockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    @Query("SELECT r FROM StockReservationEntity r LEFT JOIN FETCH r.items WHERE r.orderId = :orderId")
    Optional<StockReservationEntity> findByOrderIdWithItems(@Param("orderId") UUID orderId);

    @Query("SELECT r FROM StockReservationEntity r LEFT JOIN FETCH r.items WHERE r.id = :id")
    Optional<StockReservationEntity> findByIdWithItems(@Param("id") UUID id);

    Optional<StockReservationEntity> findByReservationReference(String reservationReference);

    @Query("SELECT r FROM StockReservationEntity r LEFT JOIN FETCH r.items WHERE r.status = :status AND r.expiresAt < :now ORDER BY r.expiresAt ASC")
    List<StockReservationEntity> findByStatusAndExpiresAtBeforeWithItems(
            @Param("status") ReservationStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}
