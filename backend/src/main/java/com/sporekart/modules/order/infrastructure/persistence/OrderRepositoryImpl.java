package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final SpringDataJpaOrderRepository jpaRepository;

    public OrderRepositoryImpl(SpringDataJpaOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdAndCustomerId(UUID id, String customerId) {
        return jpaRepository.findByIdAndCustomerId(id, customerId).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumberAndCustomerId(String orderNumber, String customerId) {
        return jpaRepository.findByOrderNumberAndCustomerId(orderNumber, customerId).map(OrderEntity::toDomain);
    }

    @Override
    public Page<Order> findByCustomerId(String customerId, Pageable pageable) {
        return jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey) {
        return jpaRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey).map(OrderEntity::toDomain);
    }
}
