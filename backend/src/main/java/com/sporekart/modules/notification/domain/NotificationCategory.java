package com.sporekart.modules.notification.domain;

public enum NotificationCategory {
    ORDER_UPDATES(false),
    PROMOTIONAL(false),
    PAYMENT(true),
    SHIPPING(false),
    RETURN_REFUND(false),
    SECURITY(true),
    SYSTEM_ALERT(true),
    TRAINING(true);

    private final boolean mandatory;

    NotificationCategory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
