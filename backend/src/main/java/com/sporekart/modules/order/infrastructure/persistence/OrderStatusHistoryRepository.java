package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.OrderStatusHistory;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository {

    OrderStatusHistory save(OrderStatusHistory history);

    List<OrderStatusHistory> findByOrderId(UUID orderId);
}
