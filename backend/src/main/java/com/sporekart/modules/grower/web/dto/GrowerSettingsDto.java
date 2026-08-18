package com.sporekart.modules.grower.web.dto;

public record GrowerSettingsDto(
        boolean emailNotifications,
        int lowStockAlertThreshold,
        boolean autoAcknowledgeOrders,
        String preferredCarrier,
        String defaultFulfillmentLocation,
        String currency
) {}
