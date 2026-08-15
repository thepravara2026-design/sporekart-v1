package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaShipmentRepository extends JpaRepository<ShipmentEntity, UUID> {
    Optional<ShipmentEntity> findByShipmentReference(String shipmentReference);
    Optional<ShipmentEntity> findByOrderId(UUID orderId);
    Optional<ShipmentEntity> findByOrderReference(String orderReference);
    Optional<ShipmentEntity> findByAwb(String awb);
    Optional<ShipmentEntity> findByProviderShipmentId(String providerShipmentId);

    Page<ShipmentEntity> findByCustomerId(String customerId, Pageable pageable);

    @Query("SELECT s FROM ShipmentEntity s WHERE (:status IS NULL OR s.status = :status)")
    Page<ShipmentEntity> findAllWithFilter(@Param("status") ShipmentStatus status, Pageable pageable);

    List<ShipmentEntity> findByStatusIn(List<ShipmentStatus> statuses);
}
