package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipping_webhook_events")
public class ShippingWebhookEventEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private ShipmentProviderType provider;

    @Column(name = "provider_event_id", nullable = false)
    private String providerEventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ShippingWebhookEventEntity() {}

    public ShippingWebhookEventEntity(
            UUID id,
            ShipmentProviderType provider,
            String providerEventId,
            String eventType,
            String payload,
            Instant processedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.provider = provider;
        this.providerEventId = providerEventId;
        this.eventType = eventType;
        this.payload = payload;
        this.processedAt = processedAt != null ? processedAt : Instant.now();
    }

    public UUID getId() { return id; }
    public ShipmentProviderType getProvider() { return provider; }
    public String getProviderEventId() { return providerEventId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getProcessedAt() { return processedAt; }
}
