package com.sporekart.modules.grower.web.dto;

import com.sporekart.modules.grower.domain.GrowerProfile;
import com.sporekart.modules.grower.domain.GrowerStatus;

public record GrowerProfileDto(
        String id,
        String userId,
        String businessName,
        String contactEmail,
        String contactPhone,
        String farmAddress,
        GrowerStatus status,
        int lowStockAlertThreshold,
        boolean autoAcknowledgeOrders,
        String preferredCarrier,
        String defaultFulfillmentLocation
) {
    public static GrowerProfileDto fromDomain(GrowerProfile profile) {
        return new GrowerProfileDto(
                profile.getId(),
                profile.getUserId(),
                profile.getBusinessName(),
                profile.getContactEmail(),
                profile.getContactPhone(),
                profile.getFarmAddress(),
                profile.getStatus(),
                profile.getLowStockAlertThreshold(),
                profile.isAutoAcknowledgeOrders(),
                profile.getPreferredCarrier(),
                profile.getDefaultFulfillmentLocation()
        );
    }
}
