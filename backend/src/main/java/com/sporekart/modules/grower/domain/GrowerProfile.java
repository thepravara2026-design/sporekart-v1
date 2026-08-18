package com.sporekart.modules.grower.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public class GrowerProfile {

    private final String id;
    private final String userId;
    private String businessName;
    private String contactEmail;
    private String contactPhone;
    private String farmAddress;
    private GrowerStatus status;
    private int lowStockAlertThreshold;
    private boolean autoAcknowledgeOrders;
    private String preferredCarrier;
    private String defaultFulfillmentLocation;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public GrowerProfile(
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
            String defaultFulfillmentLocation,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Grower profile ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.businessName = businessName != null ? businessName.trim() : "Mycology Farm";
        this.contactEmail = contactEmail != null ? contactEmail.trim() : "grower@sporekart.com";
        this.contactPhone = contactPhone;
        this.farmAddress = farmAddress;
        this.status = status != null ? status : GrowerStatus.ACTIVE;
        this.lowStockAlertThreshold = lowStockAlertThreshold >= 0 ? lowStockAlertThreshold : 5;
        this.autoAcknowledgeOrders = autoAcknowledgeOrders;
        this.preferredCarrier = preferredCarrier != null ? preferredCarrier : "Standard Express";
        this.defaultFulfillmentLocation = defaultFulfillmentLocation != null ? defaultFulfillmentLocation : "Main Facility";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public static GrowerProfile createDefault(String userId, String email, String businessName) {
        String id = "grower-profile-" + userId;
        OffsetDateTime now = OffsetDateTime.now();
        return new GrowerProfile(id, userId, businessName, email, null, null, GrowerStatus.ACTIVE, 5, false, "Standard Express", "Main Facility", now, now);
    }

    public void updateProfile(String businessName, String contactEmail, String contactPhone, String farmAddress) {
        if (businessName != null && !businessName.isBlank()) {
            this.businessName = businessName.trim();
        }
        if (contactEmail != null && !contactEmail.isBlank()) {
            this.contactEmail = contactEmail.trim();
        }
        this.contactPhone = contactPhone;
        this.farmAddress = farmAddress;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateSettings(int lowStockAlertThreshold, boolean autoAcknowledgeOrders, String preferredCarrier, String defaultFulfillmentLocation) {
        this.lowStockAlertThreshold = Math.max(0, lowStockAlertThreshold);
        this.autoAcknowledgeOrders = autoAcknowledgeOrders;
        if (preferredCarrier != null && !preferredCarrier.isBlank()) {
            this.preferredCarrier = preferredCarrier.trim();
        }
        if (defaultFulfillmentLocation != null && !defaultFulfillmentLocation.isBlank()) {
            this.defaultFulfillmentLocation = defaultFulfillmentLocation.trim();
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getBusinessName() { return businessName; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public String getFarmAddress() { return farmAddress; }
    public GrowerStatus getStatus() { return status; }
    public int getLowStockAlertThreshold() { return lowStockAlertThreshold; }
    public boolean isAutoAcknowledgeOrders() { return autoAcknowledgeOrders; }
    public String getPreferredCarrier() { return preferredCarrier; }
    public String getDefaultFulfillmentLocation() { return defaultFulfillmentLocation; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
