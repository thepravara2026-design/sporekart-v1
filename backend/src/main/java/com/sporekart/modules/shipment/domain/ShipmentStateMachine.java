package com.sporekart.modules.shipment.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ShipmentStateMachine {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(ShipmentStatus.CREATED, EnumSet.of(ShipmentStatus.READY_FOR_BOOKING, ShipmentStatus.CANCELLED)),
            Map.entry(ShipmentStatus.READY_FOR_BOOKING, EnumSet.of(ShipmentStatus.BOOKING_PENDING, ShipmentStatus.BOOKED, ShipmentStatus.CANCELLED)),
            Map.entry(ShipmentStatus.BOOKING_PENDING, EnumSet.of(ShipmentStatus.BOOKED, ShipmentStatus.EXCEPTION, ShipmentStatus.CANCELLED)),
            Map.entry(ShipmentStatus.BOOKED, EnumSet.of(ShipmentStatus.PICKUP_SCHEDULED, ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.PICKUP_SCHEDULED, EnumSet.of(ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.PICKED_UP, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.IN_TRANSIT, EnumSet.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED, ShipmentStatus.DELIVERY_FAILED, ShipmentStatus.RTO_INITIATED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.OUT_FOR_DELIVERY, EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.DELIVERY_FAILED, ShipmentStatus.RTO_INITIATED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.DELIVERY_FAILED, EnumSet.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.IN_TRANSIT, ShipmentStatus.RTO_INITIATED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.RTO_INITIATED, EnumSet.of(ShipmentStatus.RTO_IN_TRANSIT, ShipmentStatus.RTO_DELIVERED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.RTO_IN_TRANSIT, EnumSet.of(ShipmentStatus.RTO_DELIVERED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.EXCEPTION, EnumSet.of(ShipmentStatus.BOOKING_PENDING, ShipmentStatus.BOOKED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED, ShipmentStatus.EXCEPTION)),
            Map.entry(ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class)),
            Map.entry(ShipmentStatus.CANCELLED, EnumSet.noneOf(ShipmentStatus.class)),
            Map.entry(ShipmentStatus.RTO_DELIVERED, EnumSet.noneOf(ShipmentStatus.class))
    );

    public static boolean isTransitionAllowed(ShipmentStatus current, ShipmentStatus next) {
        if (current == next) {
            return true; // Idempotent same-state check
        }
        Set<ShipmentStatus> allowed = ALLOWED_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(next);
    }

    public static void validateTransition(ShipmentStatus current, ShipmentStatus next, String reason) {
        if (!isTransitionAllowed(current, next)) {
            throw new InvalidShipmentStateTransitionException(
                    current,
                    next,
                    reason != null ? reason : "Invalid shipment lifecycle state transition"
            );
        }
    }

    public static boolean isStateRegression(ShipmentStatus current, ShipmentStatus incoming) {
        if (current == incoming) return false;
        // Priority rank for progress check
        int currentRank = getProgressRank(current);
        int incomingRank = getProgressRank(incoming);
        return currentRank > incomingRank;
    }

    private static int getProgressRank(ShipmentStatus status) {
        return switch (status) {
            case CREATED -> 1;
            case READY_FOR_BOOKING -> 2;
            case BOOKING_PENDING -> 3;
            case BOOKED -> 4;
            case PICKUP_SCHEDULED -> 5;
            case PICKED_UP -> 6;
            case IN_TRANSIT -> 7;
            case OUT_FOR_DELIVERY -> 8;
            case DELIVERED -> 10;
            case DELIVERY_FAILED -> 7;
            case RTO_INITIATED -> 9;
            case RTO_IN_TRANSIT -> 9;
            case RTO_DELIVERED -> 10;
            case EXCEPTION -> 4;
            case CANCELLED -> 10;
        };
    }
}
