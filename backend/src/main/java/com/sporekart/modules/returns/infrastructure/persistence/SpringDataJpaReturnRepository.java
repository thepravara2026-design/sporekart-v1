package com.sporekart.modules.returns.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJpaReturnRepository extends JpaRepository<ReturnEntity, UUID> {
    Optional<ReturnEntity> findByReturnReference(String returnReference);
    List<ReturnEntity> findByOrderId(UUID orderId);
    List<ReturnEntity> findByCustomerId(String customerId);
    List<ReturnEntity> findByOrderReference(String orderReference);
}
