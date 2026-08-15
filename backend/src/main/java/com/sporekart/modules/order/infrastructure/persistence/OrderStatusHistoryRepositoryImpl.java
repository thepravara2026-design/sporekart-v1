package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.OrderStatusHistory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OrderStatusHistoryRepositoryImpl implements OrderStatusHistoryRepository {

    private final SpringDataJpaOrderStatusHistoryRepository jpaRepository;

    public OrderStatusHistoryRepositoryImpl(SpringDataJpaOrderStatusHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderStatusHistory save(OrderStatusHistory history) {
        OrderStatusHistoryEntity entity = OrderStatusHistoryEntity.fromDomain(history);
        OrderStatusHistoryEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<OrderStatusHistory> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(OrderStatusHistoryEntity::toDomain)
                .toList();
    }
}
