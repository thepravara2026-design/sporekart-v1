package com.sporekart.modules.support.domain;

import java.util.*;

public class SupportStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TicketStatus.OPEN, Set.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TicketStatus.ASSIGNED, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.WAITING_FOR_CUSTOMER, TicketStatus.ESCALATED, TicketStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TicketStatus.IN_PROGRESS, Set.of(TicketStatus.WAITING_FOR_CUSTOMER, TicketStatus.WAITING_FOR_INTERNAL, TicketStatus.ESCALATED, TicketStatus.RESOLVED));
        ALLOWED_TRANSITIONS.put(TicketStatus.WAITING_FOR_CUSTOMER, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TicketStatus.WAITING_FOR_INTERNAL, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.ESCALATED));
        ALLOWED_TRANSITIONS.put(TicketStatus.ESCALATED, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
        ALLOWED_TRANSITIONS.put(TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED, TicketStatus.REOPENED));
        ALLOWED_TRANSITIONS.put(TicketStatus.REOPENED, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.ESCALATED, TicketStatus.RESOLVED));
        ALLOWED_TRANSITIONS.put(TicketStatus.CLOSED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(TicketStatus.CANCELLED, Collections.emptySet());
    }

    public static boolean isValidTransition(TicketStatus current, TicketStatus target) {
        if (current == target) {
            return true;
        }
        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }

    public static void validateTransition(TicketStatus current, TicketStatus target) {
        if (!isValidTransition(current, target)) {
            throw new IllegalStateException("Invalid support ticket status transition from " + current + " to " + target);
        }
    }
}
