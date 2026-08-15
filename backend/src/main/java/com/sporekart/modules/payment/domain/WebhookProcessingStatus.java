package com.sporekart.modules.payment.domain;

public enum WebhookProcessingStatus {
    RECEIVED,
    PROCESSED,
    FAILED,
    IGNORED,
    DUPLICATE
}
