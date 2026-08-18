package com.sporekart.modules.grower.infrastructure.persistence;

import com.sporekart.modules.grower.domain.GrowerProfile;
import com.sporekart.modules.grower.domain.GrowerStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "grower_profiles")
public class GrowerProfileEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "farm_address", columnDefinition = "TEXT")
    private String farmAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private GrowerStatus status;

    @Column(name = "low_stock_alert_threshold", nullable = false)
    private int lowStockAlertThreshold;

    @Column(name = "auto_acknowledge_orders", nullable = false)
    private boolean autoAcknowledgeOrders;

    @Column(name = "preferred_carrier", length = 100)
    private String preferredCarrier;

    @Column(name = "default_fulfillment_location", length = 255)
    private String defaultFulfillmentLocation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public GrowerProfileEntity() {}

    public GrowerProfileEntity(
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
        this.id = id;
        this.userId = userId;
        this.businessName = businessName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.farmAddress = farmAddress;
        this.status = status;
        this.lowStockAlertThreshold = lowStockAlertThreshold;
        this.autoAcknowledgeOrders = autoAcknowledgeOrders;
        this.preferredCarrier = preferredCarrier;
        this.defaultFulfillmentLocation = defaultFulfillmentLocation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GrowerProfileEntity fromDomain(GrowerProfile profile) {
        if (profile == null) return null;
        return new GrowerProfileEntity(
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
                profile.getDefaultFulfillmentLocation(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public GrowerProfile toDomain() {
        return new GrowerProfile(
                id,
                userId,
                businessName,
                contactEmail,
                contactPhone,
                farmAddress,
                status,
                lowStockAlertThreshold,
                autoAcknowledgeOrders,
                preferredCarrier,
                defaultFulfillmentLocation,
                createdAt,
                updatedAt
        );
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
