package com.sporekart.modules.order.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaOrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.customerId = :customerId")
    Optional<OrderEntity> findByIdAndCustomerId(@Param("id") UUID id, @Param("customerId") String customerId);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber AND o.customerId = :customerId")
    Optional<OrderEntity> findByOrderNumberAndCustomerId(@Param("orderNumber") String orderNumber, @Param("customerId") String customerId);

    Page<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    Optional<OrderEntity> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
}
