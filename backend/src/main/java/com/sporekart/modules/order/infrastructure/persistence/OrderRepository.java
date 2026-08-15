package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByIdAndCustomerId(UUID id, String customerId);

    Optional<Order> findByOrderNumberAndCustomerId(String orderNumber, String customerId);

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Optional<Order> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
}
