package com.sporekart.modules.support.domain;

public enum TicketStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_CUSTOMER,
    WAITING_FOR_INTERNAL,
    ESCALATED,
    RESOLVED,
    CLOSED,
    REOPENED,
    CANCELLED;

    public boolean isTerminal() {
        return this == CLOSED || this == CANCELLED;
    }
}
