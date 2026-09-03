package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(UUID id);
    Optional<Shipment> findByShipmentReference(String shipmentReference);
    Optional<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findByOrderReference(String orderReference);
    Optional<Shipment> findByAwb(String awb);
    Optional<Shipment> findByProviderShipmentId(String providerShipmentId);
    Page<Shipment> findByCustomerId(String customerId, Pageable pageable);
    Page<Shipment> findAll(ShipmentStatus statusFilter, Pageable pageable);
    List<Shipment> findActiveShipmentsForReconciliation();
    List<Shipment> findAllByStatus(ShipmentStatus status);
    List<Shipment> findAllByGrowerId(String growerId);

    boolean existsWebhookEvent(ShipmentProviderType provider, String providerEventId);
    void saveWebhookEvent(ShippingWebhookEventEntity entity);
}
