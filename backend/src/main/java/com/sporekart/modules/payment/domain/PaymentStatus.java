package com.sporekart.modules.payment.domain;

public enum PaymentStatus {
    CREATED,
    PENDING,
    AUTHORIZED,
    SUCCESS,
    FAILED,
    CANCELLED,
    EXPIRED;

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == EXPIRED;
    }
}
