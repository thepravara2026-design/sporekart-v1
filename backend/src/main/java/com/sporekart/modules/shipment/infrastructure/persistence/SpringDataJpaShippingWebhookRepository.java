package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaShippingWebhookRepository extends JpaRepository<ShippingWebhookEventEntity, UUID> {
    boolean existsByProviderAndProviderEventId(ShipmentProviderType provider, String providerEventId);
    Optional<ShippingWebhookEventEntity> findByProviderAndProviderEventId(ShipmentProviderType provider, String providerEventId);
}
