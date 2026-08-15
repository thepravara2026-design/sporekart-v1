package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ShipmentRepositoryImpl implements ShipmentRepository {

    private final SpringDataJpaShipmentRepository springDataRepository;
    private final SpringDataJpaShippingWebhookRepository webhookRepository;

    public ShipmentRepositoryImpl(
            SpringDataJpaShipmentRepository springDataRepository,
            SpringDataJpaShippingWebhookRepository webhookRepository
    ) {
        this.springDataRepository = springDataRepository;
        this.webhookRepository = webhookRepository;
    }

    @Override
    public Shipment save(Shipment shipment) {
        Optional<ShipmentEntity> existing = springDataRepository.findById(shipment.getId());
        ShipmentEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.updateFromDomain(shipment);
        } else {
            entity = ShipmentEntity.fromDomain(shipment);
        }
        ShipmentEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return springDataRepository.findById(id).map(ShipmentEntity::toDomain);
    }

    @Override
    public Optional<Shipment> findByShipmentReference(String shipmentReference) {
        return springDataRepository.findByShipmentReference(shipmentReference).map(ShipmentEntity::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return springDataRepository.findByOrderId(orderId).map(ShipmentEntity::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderReference(String orderReference) {
        return springDataRepository.findByOrderReference(orderReference).map(ShipmentEntity::toDomain);
    }

    @Override
    public Optional<Shipment> findByAwb(String awb) {
        return springDataRepository.findByAwb(awb).map(ShipmentEntity::toDomain);
    }

    @Override
    public Optional<Shipment> findByProviderShipmentId(String providerShipmentId) {
        return springDataRepository.findByProviderShipmentId(providerShipmentId).map(ShipmentEntity::toDomain);
    }

    @Override
    public Page<Shipment> findByCustomerId(String customerId, Pageable pageable) {
        return springDataRepository.findByCustomerId(customerId, pageable).map(ShipmentEntity::toDomain);
    }

    @Override
    public Page<Shipment> findAll(ShipmentStatus statusFilter, Pageable pageable) {
        return springDataRepository.findAllWithFilter(statusFilter, pageable).map(ShipmentEntity::toDomain);
    }

    @Override
    public List<Shipment> findActiveShipmentsForReconciliation() {
        List<ShipmentStatus> activeStatuses = List.of(
                ShipmentStatus.BOOKED,
                ShipmentStatus.PICKUP_SCHEDULED,
                ShipmentStatus.PICKED_UP,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY,
                ShipmentStatus.DELIVERY_FAILED
        );
        return springDataRepository.findByStatusIn(activeStatuses).stream()
                .map(ShipmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsWebhookEvent(ShipmentProviderType provider, String providerEventId) {
        return webhookRepository.existsByProviderAndProviderEventId(provider, providerEventId);
    }

    @Override
    public void saveWebhookEvent(ShippingWebhookEventEntity entity) {
        if (entity.getProviderEventId() != null && webhookRepository.existsByProviderAndProviderEventId(entity.getProvider(), entity.getProviderEventId())) {
            return;
        }
        webhookRepository.save(entity);
    }
}
