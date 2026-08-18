package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentItem;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShipmentStatusHistory;
import com.sporekart.modules.shipment.domain.ShipmentTrackingEvent;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "shipments")
public class ShipmentEntity {

    @Id
    private UUID id;

    @Column(name = "shipment_reference", nullable = false, unique = true)
    private String shipmentReference;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "order_reference", nullable = false)
    private String orderReference;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "grower_id", length = 100)
    private String growerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private ShipmentProviderType provider;

    @Column(name = "provider_shipment_id")
    private String providerShipmentId;

    @Column(name = "awb")
    private String awb;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "courier_code")
    private String courierCode;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "length_mm")
    private Integer lengthMm;

    @Column(name = "width_mm")
    private Integer widthMm;

    @Column(name = "height_mm")
    private Integer heightMm;

    @Column(name = "declared_value")
    private BigDecimal declaredValue;

    @Column(name = "shipping_name", nullable = false)
    private String shippingName;

    @Column(name = "shipping_phone", nullable = false)
    private String shippingPhone;

    @Column(name = "shipping_address_line1", nullable = false)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2")
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false)
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false)
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false)
    private String shippingCountry;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "booked_at")
    private Instant bookedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShipmentItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShipmentStatusHistoryEntity> statusHistories = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShipmentTrackingEventEntity> trackingEvents = new ArrayList<>();

    public ShipmentEntity() {}

    public static ShipmentEntity fromDomain(Shipment domain) {
        ShipmentEntity entity = new ShipmentEntity();
        entity.id = domain.getId();
        entity.shipmentReference = domain.getShipmentReference();
        entity.orderId = domain.getOrderId();
        entity.orderReference = domain.getOrderReference();
        entity.customerId = domain.getCustomerId();
        entity.growerId = domain.getGrowerId();
        entity.status = domain.getStatus();
        entity.provider = domain.getProvider();
        entity.providerShipmentId = domain.getProviderShipmentId();
        entity.awb = domain.getAwb();
        entity.trackingNumber = domain.getTrackingNumber();
        entity.courierName = domain.getCourierName();
        entity.courierCode = domain.getCourierCode();

        if (domain.getPackageDetails() != null) {
            entity.weightGrams = domain.getPackageDetails().getWeightGrams();
            entity.lengthMm = domain.getPackageDetails().getLengthMm();
            entity.widthMm = domain.getPackageDetails().getWidthMm();
            entity.heightMm = domain.getPackageDetails().getHeightMm();
            entity.declaredValue = domain.getPackageDetails().getDeclaredValue();
        }

        if (domain.getShippingAddress() != null) {
            entity.shippingName = domain.getShippingAddress().getRecipientName();
            entity.shippingPhone = domain.getShippingAddress().getPhone();
            entity.shippingAddressLine1 = domain.getShippingAddress().getAddressLine1();
            entity.shippingAddressLine2 = domain.getShippingAddress().getAddressLine2();
            entity.shippingCity = domain.getShippingAddress().getCity();
            entity.shippingState = domain.getShippingAddress().getState();
            entity.shippingPostalCode = domain.getShippingAddress().getPostalCode();
            entity.shippingCountry = domain.getShippingAddress().getCountry();
        }

        entity.estimatedDeliveryAt = domain.getEstimatedDeliveryAt();
        entity.bookedAt = domain.getBookedAt();
        entity.pickedUpAt = domain.getPickedUpAt();
        entity.deliveredAt = domain.getDeliveredAt();
        entity.version = (domain.getVersion() != null && domain.getVersion() > 0) ? domain.getVersion() : null;
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();

        if (domain.getItems() != null) {
            entity.items = domain.getItems().stream()
                    .map(item -> ShipmentItemEntity.fromDomain(item, entity))
                    .collect(Collectors.toList());
        }

        if (domain.getStatusHistories() != null) {
            entity.statusHistories = domain.getStatusHistories().stream()
                    .map(history -> ShipmentStatusHistoryEntity.fromDomain(history, entity))
                    .collect(Collectors.toList());
        }

        if (domain.getTrackingEvents() != null) {
            entity.trackingEvents = domain.getTrackingEvents().stream()
                    .map(event -> ShipmentTrackingEventEntity.fromDomain(event, entity))
                    .collect(Collectors.toList());
        }

        return entity;
    }

    public void updateFromDomain(Shipment domain) {
        this.status = domain.getStatus();
        this.providerShipmentId = domain.getProviderShipmentId();
        this.awb = domain.getAwb();
        this.trackingNumber = domain.getTrackingNumber();
        this.courierName = domain.getCourierName();
        this.courierCode = domain.getCourierCode();
        this.estimatedDeliveryAt = domain.getEstimatedDeliveryAt();
        this.bookedAt = domain.getBookedAt();
        this.pickedUpAt = domain.getPickedUpAt();
        this.deliveredAt = domain.getDeliveredAt();
        this.updatedAt = domain.getUpdatedAt();

        if (domain.getStatusHistories() != null) {
            for (ShipmentStatusHistory hist : domain.getStatusHistories()) {
                boolean exists = this.statusHistories.stream().anyMatch(h -> h.toDomain().getId().equals(hist.getId()));
                if (!exists) {
                    this.statusHistories.add(ShipmentStatusHistoryEntity.fromDomain(hist, this));
                }
            }
        }

        if (domain.getTrackingEvents() != null) {
            for (ShipmentTrackingEvent event : domain.getTrackingEvents()) {
                boolean exists = this.trackingEvents.stream().anyMatch(e -> e.toDomain().getId().equals(event.getId())
                        || (event.getProviderEventId() != null && event.getProviderEventId().equals(e.toDomain().getProviderEventId())));
                if (!exists) {
                    this.trackingEvents.add(ShipmentTrackingEventEntity.fromDomain(event, this));
                }
            }
        }
    }

    public Shipment toDomain() {
        PackageDetails pkg = new PackageDetails(weightGrams, lengthMm, widthMm, heightMm, declaredValue);
        ShippingAddressSnapshot addr = new ShippingAddressSnapshot(
                shippingName, shippingPhone, shippingAddressLine1, shippingAddressLine2, shippingCity, shippingState, shippingPostalCode, shippingCountry
        );
        List<ShipmentItem> domainItems = items != null ? items.stream().map(ShipmentItemEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>();
        List<ShipmentStatusHistory> domainHist = statusHistories != null ? statusHistories.stream().map(ShipmentStatusHistoryEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>();
        List<ShipmentTrackingEvent> domainTracking = trackingEvents != null ? trackingEvents.stream().map(ShipmentTrackingEventEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>();

        return new Shipment(
                id,
                shipmentReference,
                orderId,
                orderReference,
                customerId,
                growerId,
                status,
                provider,
                providerShipmentId,
                awb,
                trackingNumber,
                courierName,
                courierCode,
                pkg,
                addr,
                domainItems,
                estimatedDeliveryAt,
                bookedAt,
                pickedUpAt,
                deliveredAt,
                domainHist,
                domainTracking,
                version,
                createdAt,
                updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public String getShipmentReference() { return shipmentReference; }
    public UUID getOrderId() { return orderId; }
    public String getOrderReference() { return orderReference; }
    public String getCustomerId() { return customerId; }
    public String getGrowerId() { return growerId; }
    public void setGrowerId(String growerId) { this.growerId = growerId; }
    public ShipmentStatus getStatus() { return status; }
    public ShipmentProviderType getProvider() { return provider; }
    public String getProviderShipmentId() { return providerShipmentId; }
    public String getAwb() { return awb; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getCourierName() { return courierName; }
    public String getCourierCode() { return courierCode; }
    public Long getVersion() { return version; }
}
