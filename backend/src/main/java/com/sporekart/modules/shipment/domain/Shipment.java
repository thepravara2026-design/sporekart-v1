package com.sporekart.modules.shipment.domain;

import com.sporekart.modules.order.domain.OrderActorType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Shipment {
    private final UUID id;
    private final String shipmentReference;
    private final UUID orderId;
    private final String orderReference;
    private final String customerId;
    private ShipmentStatus status;
    private final ShipmentProviderType provider;

    private String providerShipmentId;
    private String awb;
    private String trackingNumber;
    private String courierName;
    private String courierCode;

    private final PackageDetails packageDetails;
    private final ShippingAddressSnapshot shippingAddress;
    private final List<ShipmentItem> items;

    private Instant estimatedDeliveryAt;
    private Instant bookedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;

    private final List<ShipmentStatusHistory> statusHistories = new ArrayList<>();
    private final List<ShipmentTrackingEvent> trackingEvents = new ArrayList<>();

    private Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    public Shipment(
            UUID id,
            String shipmentReference,
            UUID orderId,
            String orderReference,
            String customerId,
            ShipmentStatus status,
            ShipmentProviderType provider,
            String providerShipmentId,
            String awb,
            String trackingNumber,
            String courierName,
            String courierCode,
            PackageDetails packageDetails,
            ShippingAddressSnapshot shippingAddress,
            List<ShipmentItem> items,
            Instant estimatedDeliveryAt,
            Instant bookedAt,
            Instant pickedUpAt,
            Instant deliveredAt,
            List<ShipmentStatusHistory> statusHistories,
            List<ShipmentTrackingEvent> trackingEvents,
            Long version,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, shipmentReference, orderId, orderReference, customerId, null, status, provider, providerShipmentId, awb, trackingNumber, courierName, courierCode, packageDetails, shippingAddress, items, estimatedDeliveryAt, bookedAt, pickedUpAt, deliveredAt, statusHistories, trackingEvents, version, createdAt, updatedAt);
    }

    public Shipment(
            UUID id,
            String shipmentReference,
            UUID orderId,
            String orderReference,
            String customerId,
            String growerId,
            ShipmentStatus status,
            ShipmentProviderType provider,
            String providerShipmentId,
            String awb,
            String trackingNumber,
            String courierName,
            String courierCode,
            PackageDetails packageDetails,
            ShippingAddressSnapshot shippingAddress,
            List<ShipmentItem> items,
            Instant estimatedDeliveryAt,
            Instant bookedAt,
            Instant pickedUpAt,
            Instant deliveredAt,
            List<ShipmentStatusHistory> statusHistories,
            List<ShipmentTrackingEvent> trackingEvents,
            Long version,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.shipmentReference = Objects.requireNonNull(shipmentReference, "shipmentReference required");
        this.orderId = Objects.requireNonNull(orderId, "orderId required");
        this.orderReference = Objects.requireNonNull(orderReference, "orderReference required");
        this.customerId = Objects.requireNonNull(customerId, "customerId required");
        this.growerId = growerId;
        this.status = status != null ? status : ShipmentStatus.CREATED;
        this.provider = provider != null ? provider : ShipmentProviderType.MOCK;
        this.providerShipmentId = providerShipmentId;
        this.awb = awb;
        this.trackingNumber = trackingNumber;
        this.courierName = courierName;
        this.courierCode = courierCode;
        this.packageDetails = Objects.requireNonNull(packageDetails, "packageDetails required");
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "shippingAddress required");
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.estimatedDeliveryAt = estimatedDeliveryAt;
        this.bookedAt = bookedAt;
        this.pickedUpAt = pickedUpAt;
        this.deliveredAt = deliveredAt;
        if (statusHistories != null) {
            this.statusHistories.addAll(statusHistories);
        }
        if (trackingEvents != null) {
            this.trackingEvents.addAll(trackingEvents);
        }
        this.version = version;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();

        if (this.statusHistories.isEmpty()) {
            recordStatusChange(null, this.status, "Initial shipment creation", OrderActorType.SYSTEM, "SYSTEM", null, null);
        }
    }

    public static Shipment create(
            String shipmentReference,
            UUID orderId,
            String orderReference,
            String customerId,
            ShipmentProviderType provider,
            PackageDetails packageDetails,
            ShippingAddressSnapshot shippingAddress,
            List<ShipmentItem> items
    ) {
        return new Shipment(
                UUID.randomUUID(),
                shipmentReference,
                orderId,
                orderReference,
                customerId,
                ShipmentStatus.CREATED,
                provider,
                null, null, null, null, null,
                packageDetails,
                shippingAddress,
                items,
                null, null, null, null,
                null, null,
                0L,
                Instant.now(),
                Instant.now()
        );
    }

    public void markReadyForBooking(OrderActorType actorType, String actorId) {
        transitionTo(ShipmentStatus.READY_FOR_BOOKING, "Ready for provider booking", actorType, actorId, null, null);
    }

    public void markBookingPending(OrderActorType actorType, String actorId) {
        transitionTo(ShipmentStatus.BOOKING_PENDING, "Booking initiated with shipping provider", actorType, actorId, null, null);
    }

    public void markBooked(
            String providerShipmentId,
            String awb,
            String trackingNumber,
            String courierName,
            String courierCode,
            Instant estimatedDeliveryAt,
            OrderActorType actorType,
            String actorId
    ) {
        this.providerShipmentId = Objects.requireNonNull(providerShipmentId, "providerShipmentId required");
        this.awb = Objects.requireNonNull(awb, "awb required");
        this.trackingNumber = trackingNumber != null ? trackingNumber : awb;
        this.courierName = courierName != null ? courierName : "Mock Express";
        this.courierCode = courierCode != null ? courierCode : "MOCK_EXPRESS";
        this.estimatedDeliveryAt = estimatedDeliveryAt != null ? estimatedDeliveryAt : Instant.now().plusSeconds(86400 * 3);
        this.bookedAt = Instant.now();

        transitionTo(ShipmentStatus.BOOKED, "Shipment successfully booked with provider", actorType, actorId, null, null);
    }

    public void markPickedUp(Instant pickedUpAt, OrderActorType actorType, String actorId, String providerEventId) {
        this.pickedUpAt = pickedUpAt != null ? pickedUpAt : Instant.now();
        transitionTo(ShipmentStatus.PICKED_UP, "Package picked up by courier", actorType, actorId, providerEventId, null);
    }

    public void markInTransit(OrderActorType actorType, String actorId, String providerEventId) {
        transitionTo(ShipmentStatus.IN_TRANSIT, "Shipment in transit to destination", actorType, actorId, providerEventId, null);
    }

    public void markOutForDelivery(OrderActorType actorType, String actorId, String providerEventId) {
        transitionTo(ShipmentStatus.OUT_FOR_DELIVERY, "Shipment out for delivery to customer", actorType, actorId, providerEventId, null);
    }

    public void markDelivered(Instant deliveredAt, OrderActorType actorType, String actorId, String providerEventId) {
        this.deliveredAt = deliveredAt != null ? deliveredAt : Instant.now();
        transitionTo(ShipmentStatus.DELIVERED, "Shipment delivered to recipient", actorType, actorId, providerEventId, null);
    }

    public void markDeliveryFailed(String reason, OrderActorType actorType, String actorId, String providerEventId) {
        transitionTo(ShipmentStatus.DELIVERY_FAILED, reason != null ? reason : "Delivery attempt failed", actorType, actorId, providerEventId, null);
    }

    public void markCancelled(String reason, OrderActorType actorType, String actorId) {
        transitionTo(ShipmentStatus.CANCELLED, reason != null ? reason : "Shipment cancelled", actorType, actorId, null, null);
    }

    public void markRtoInitiated(String reason, OrderActorType actorType, String actorId, String providerEventId) {
        transitionTo(ShipmentStatus.RTO_INITIATED, reason != null ? reason : "Return to origin initiated", actorType, actorId, providerEventId, null);
    }

    public void addTrackingEvent(
            String providerEventId,
            String providerStatus,
            ShipmentStatus normalizedStatus,
            String description,
            String location,
            Instant occurredAt
    ) {
        boolean exists = trackingEvents.stream().anyMatch(e -> e.getProviderEventId().equals(providerEventId));
        if (exists) {
            return; // Idempotent event check
        }
        ShipmentTrackingEvent event = new ShipmentTrackingEvent(
                UUID.randomUUID(),
                this.id,
                providerEventId,
                providerStatus,
                normalizedStatus,
                description,
                location,
                occurredAt,
                Instant.now(),
                Instant.now()
        );
        this.trackingEvents.add(event);

        // Transition status if not regression
        if (!ShipmentStateMachine.isStateRegression(this.status, normalizedStatus) && this.status != normalizedStatus) {
            transitionTo(normalizedStatus, description, OrderActorType.SYSTEM, "PROVIDER", providerEventId, null);
        }
    }

    private void transitionTo(
            ShipmentStatus nextStatus,
            String reason,
            OrderActorType actorType,
            String actorId,
            String providerEventId,
            String correlationId
    ) {
        if (this.status == nextStatus) {
            return; // Idempotent same-state call
        }
        if (ShipmentStateMachine.isStateRegression(this.status, nextStatus)) {
            // Ignore state regression attempt (e.g., out-of-order webhook)
            return;
        }
        ShipmentStateMachine.validateTransition(this.status, nextStatus, reason);
        ShipmentStatus prev = this.status;
        this.status = nextStatus;
        this.updatedAt = Instant.now();

        recordStatusChange(prev, nextStatus, reason, actorType, actorId, providerEventId, correlationId);
    }

    private void recordStatusChange(
            ShipmentStatus prev,
            ShipmentStatus next,
            String reason,
            OrderActorType actorType,
            String actorId,
            String providerEventId,
            String correlationId
    ) {
        ShipmentStatusHistory history = new ShipmentStatusHistory(
                UUID.randomUUID(),
                this.id,
                prev,
                next,
                reason,
                actorType,
                actorId,
                providerEventId,
                correlationId,
                Instant.now()
        );
        this.statusHistories.add(history);
    }

    // Getters
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
    public PackageDetails getPackageDetails() { return packageDetails; }
    public ShippingAddressSnapshot getShippingAddress() { return shippingAddress; }
    public List<ShipmentItem> getItems() { return Collections.unmodifiableList(items); }
    public Instant getEstimatedDeliveryAt() { return estimatedDeliveryAt; }
    public Instant getBookedAt() { return bookedAt; }
    public Instant getPickedUpAt() { return pickedUpAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public List<ShipmentStatusHistory> getStatusHistories() { return Collections.unmodifiableList(statusHistories); }
    public List<ShipmentTrackingEvent> getTrackingEvents() { return Collections.unmodifiableList(trackingEvents); }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
