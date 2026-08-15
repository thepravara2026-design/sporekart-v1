package com.sporekart.modules.support.domain;

public enum TicketPriority {
    LOW(48, 72),
    NORMAL(24, 48),
    HIGH(12, 24),
    URGENT(4, 12);

    private final int firstResponseHours;
    private final int resolutionHours;

    TicketPriority(int firstResponseHours, int resolutionHours) {
        this.firstResponseHours = firstResponseHours;
        this.resolutionHours = resolutionHours;
    }

    public int getFirstResponseHours() {
        return firstResponseHours;
    }

    public int getResolutionHours() {
        return resolutionHours;
    }
}
