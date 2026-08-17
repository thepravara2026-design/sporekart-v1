package com.sporekart.modules.notification.domain;

public enum ProviderFailureCategory {
    TRANSIENT,
    RATE_LIMITED,
    TIMEOUT,
    PERMANENT,
    AUTHENTICATION,
    CONFIGURATION,
    UNKNOWN
}
