package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockReservation;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final SpringDataJpaStockReservationRepository jpaRepository;

    public ReservationRepositoryImpl(SpringDataJpaStockReservationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        StockReservationEntity entity = StockReservationEntity.fromDomain(reservation);
        StockReservationEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<StockReservation> findById(UUID id) {
        return jpaRepository.findByIdWithItems(id).map(StockReservationEntity::toDomain);
    }

    @Override
    public Optional<StockReservation> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderIdWithItems(orderId).map(StockReservationEntity::toDomain);
    }

    @Override
    public Optional<StockReservation> findByReservationReference(String reservationReference) {
        return jpaRepository.findByReservationReference(reservationReference).map(StockReservationEntity::toDomain);
    }

    @Override
    public List<StockReservation> findExpiredActiveReservations(OffsetDateTime now, Pageable pageable) {
        return jpaRepository.findByStatusAndExpiresAtBeforeWithItems(ReservationStatus.ACTIVE, now, pageable).stream()
                .map(StockReservationEntity::toDomain)
                .toList();
    }
}
