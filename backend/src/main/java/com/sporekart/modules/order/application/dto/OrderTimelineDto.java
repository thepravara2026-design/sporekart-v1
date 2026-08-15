package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;

import java.util.List;
import java.util.UUID;

public record OrderTimelineDto(
        UUID orderId,
        String orderNumber,
        OrderStatus currentStatus,
        List<OrderStatusHistoryDto> history
) {
    public static OrderTimelineDto fromDomain(Order order, List<OrderStatusHistory> histories) {
        List<OrderStatusHistoryDto> dtos = histories != null
                ? histories.stream().map(OrderStatusHistoryDto::fromDomain).toList()
                : List.of();

        return new OrderTimelineDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                dtos
        );
    }
}
