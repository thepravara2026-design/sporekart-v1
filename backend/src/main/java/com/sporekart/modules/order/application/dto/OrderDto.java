package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        String orderNumber,
        String customerId,
        OrderStatus status,
        String currency,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal taxTotal,
        BigDecimal shippingFee,
        BigDecimal grandTotal,
        String idempotencyKey,
        AddressDto shippingAddress,
        String customerNotes,
        List<OrderItemDto> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static OrderDto fromDomain(Order order) {
        List<OrderItemDto> itemDtos = order.getItems() != null
                ? order.getItems().stream().map(OrderItemDto::fromDomain).toList()
                : List.of();

        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getCurrency(),
                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getTaxTotal(),
                order.getShippingFee(),
                order.getGrandTotal(),
                order.getIdempotencyKey(),
                AddressDto.fromDomain(order.getShippingAddress()),
                order.getCustomerNotes(),
                itemDtos,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
