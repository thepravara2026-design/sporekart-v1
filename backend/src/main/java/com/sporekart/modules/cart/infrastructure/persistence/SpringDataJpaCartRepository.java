package com.sporekart.modules.cart.infrastructure.persistence;

import com.sporekart.modules.cart.domain.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaCartRepository extends JpaRepository<CartEntity, UUID> {

    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId AND c.status = :status")
    Optional<CartEntity> findByCustomerIdAndStatusWithItems(
            @Param("customerId") String customerId,
            @Param("status") CartStatus status
    );

    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<CartEntity> findByIdWithItems(@Param("id") UUID id);
}
