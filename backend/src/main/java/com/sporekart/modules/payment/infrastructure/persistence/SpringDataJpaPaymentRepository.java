package com.sporekart.modules.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaPaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query("SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.attempts WHERE p.id = :id")
    Optional<PaymentEntity> findByIdWithAttempts(@Param("id") UUID id);

    @Query("SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.attempts WHERE p.paymentReference = :paymentReference")
    Optional<PaymentEntity> findByPaymentReferenceWithAttempts(@Param("paymentReference") String paymentReference);

    @Query("SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.attempts WHERE p.orderId = :orderId")
    Optional<PaymentEntity> findByOrderIdWithAttempts(@Param("orderId") UUID orderId);

    @Query("SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.attempts WHERE p.status = :status")
    java.util.List<PaymentEntity> findAllByStatus(@Param("status") com.sporekart.modules.payment.domain.PaymentStatus status);
}
