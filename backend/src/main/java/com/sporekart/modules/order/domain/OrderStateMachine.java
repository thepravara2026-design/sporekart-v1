package com.sporekart.modules.order.domain;

import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.ofEntries(
            Map.entry(OrderStatus.CREATED, EnumSet.of(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.EXPIRED)),
            Map.entry(OrderStatus.PAYMENT_PENDING, EnumSet.of(OrderStatus.PAID, OrderStatus.CONFIRMED, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED, OrderStatus.EXPIRED)),
            Map.entry(OrderStatus.PAID, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.READY_FOR_FULFILMENT, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.READY_FOR_FULFILMENT, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY)),
            Map.entry(OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED)),
            Map.entry(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.COMPLETED)),
            Map.entry(OrderStatus.PAYMENT_FAILED, EnumSet.of(OrderStatus.PAYMENT_PENDING, OrderStatus.EXPIRED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class)),
            Map.entry(OrderStatus.EXPIRED, EnumSet.noneOf(OrderStatus.class)),
            Map.entry(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class))
    );

    public static boolean isTransitionAllowed(OrderStatus current, OrderStatus target) {
        if (current == target) {
            return true; // Idempotent transition
        }
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        return allowed.contains(target);
    }

    public static void validateTransition(UUID orderId, OrderStatus current, OrderStatus target) {
        if (!isTransitionAllowed(current, target)) {
            throw new InvalidOrderStateTransitionException(orderId, current, target);
        }
    }
}
