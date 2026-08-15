package com.sporekart.modules.returns.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ReturnStateMachine {

    private static final Map<ReturnStatus, Set<ReturnStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(ReturnStatus.REQUESTED, EnumSet.of(
                    ReturnStatus.REQUESTED,
                    ReturnStatus.UNDER_REVIEW,
                    ReturnStatus.APPROVED,
                    ReturnStatus.REJECTED,
                    ReturnStatus.CANCELLED
            )),
            Map.entry(ReturnStatus.UNDER_REVIEW, EnumSet.of(
                    ReturnStatus.UNDER_REVIEW,
                    ReturnStatus.APPROVED,
                    ReturnStatus.REJECTED,
                    ReturnStatus.CANCELLED
            )),
            Map.entry(ReturnStatus.APPROVED, EnumSet.of(
                    ReturnStatus.APPROVED,
                    ReturnStatus.REVERSE_SHIPMENT_CREATED,
                    ReturnStatus.PICKUP_SCHEDULED,
                    ReturnStatus.PICKED_UP,
                    ReturnStatus.IN_TRANSIT,
                    ReturnStatus.RECEIVED,
                    ReturnStatus.CANCELLED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.REVERSE_SHIPMENT_CREATED, EnumSet.of(
                    ReturnStatus.REVERSE_SHIPMENT_CREATED,
                    ReturnStatus.PICKUP_SCHEDULED,
                    ReturnStatus.PICKUP_FAILED,
                    ReturnStatus.PICKED_UP,
                    ReturnStatus.IN_TRANSIT,
                    ReturnStatus.RECEIVED,
                    ReturnStatus.CANCELLED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.PICKUP_SCHEDULED, EnumSet.of(
                    ReturnStatus.PICKUP_SCHEDULED,
                    ReturnStatus.PICKUP_FAILED,
                    ReturnStatus.PICKED_UP,
                    ReturnStatus.IN_TRANSIT,
                    ReturnStatus.RECEIVED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.PICKUP_FAILED, EnumSet.of(
                    ReturnStatus.PICKUP_FAILED,
                    ReturnStatus.PICKUP_SCHEDULED,
                    ReturnStatus.APPROVED,
                    ReturnStatus.CANCELLED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.PICKED_UP, EnumSet.of(
                    ReturnStatus.PICKED_UP,
                    ReturnStatus.IN_TRANSIT,
                    ReturnStatus.RECEIVED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.IN_TRANSIT, EnumSet.of(
                    ReturnStatus.IN_TRANSIT,
                    ReturnStatus.RECEIVED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.RECEIVED, EnumSet.of(
                    ReturnStatus.RECEIVED,
                    ReturnStatus.INSPECTION_PENDING
            )),
            Map.entry(ReturnStatus.INSPECTION_PENDING, EnumSet.of(
                    ReturnStatus.INSPECTION_PENDING,
                    ReturnStatus.INSPECTED
            )),
            Map.entry(ReturnStatus.INSPECTED, EnumSet.of(
                    ReturnStatus.INSPECTED,
                    ReturnStatus.ACCEPTED,
                    ReturnStatus.PARTIALLY_ACCEPTED,
                    ReturnStatus.RETURN_REJECTED
            )),
            Map.entry(ReturnStatus.ACCEPTED, EnumSet.of(
                    ReturnStatus.ACCEPTED,
                    ReturnStatus.REFUND_PENDING
            )),
            Map.entry(ReturnStatus.PARTIALLY_ACCEPTED, EnumSet.of(
                    ReturnStatus.PARTIALLY_ACCEPTED,
                    ReturnStatus.REFUND_PENDING
            )),
            Map.entry(ReturnStatus.REFUND_PENDING, EnumSet.of(
                    ReturnStatus.REFUND_PENDING,
                    ReturnStatus.REFUNDED,
                    ReturnStatus.EXCEPTION
            )),
            Map.entry(ReturnStatus.EXCEPTION, EnumSet.of(
                    ReturnStatus.EXCEPTION,
                    ReturnStatus.REFUND_PENDING,
                    ReturnStatus.APPROVED
            ))
    );

    public static void validateTransition(ReturnStatus currentStatus, ReturnStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return; // Idempotent same-state call
        }

        if (currentStatus.isTerminal()) {
            throw new InvalidReturnStateTransitionException("Cannot transition from terminal state " + currentStatus + " to " + targetStatus);
        }

        Set<ReturnStatus> validTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!validTargets.contains(targetStatus)) {
            throw new InvalidReturnStateTransitionException(currentStatus, targetStatus);
        }
    }
}
