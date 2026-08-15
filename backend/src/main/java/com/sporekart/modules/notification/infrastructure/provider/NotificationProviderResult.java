package com.sporekart.modules.notification.infrastructure.provider;

public record NotificationProviderResult(
        boolean success,
        String providerMessageId,
        boolean transientFailure,
        String errorMessage
) {
    public static NotificationProviderResult success(String providerMessageId) {
        return new NotificationProviderResult(true, providerMessageId, false, null);
    }

    public static NotificationProviderResult failure(String errorMessage, boolean transientFailure) {
        return new NotificationProviderResult(false, null, transientFailure, errorMessage);
    }
}
