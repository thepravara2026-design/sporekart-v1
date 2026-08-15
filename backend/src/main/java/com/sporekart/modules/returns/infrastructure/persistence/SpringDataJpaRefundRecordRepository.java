package com.sporekart.modules.returns.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJpaRefundRecordRepository extends JpaRepository<RefundRecordEntity, UUID> {
    Optional<RefundRecordEntity> findByRefundReference(String refundReference);
    Optional<RefundRecordEntity> findByIdempotencyKey(String idempotencyKey);
    List<RefundRecordEntity> findByReturnId(UUID returnId);
    List<RefundRecordEntity> findByOrderId(UUID orderId);
}
