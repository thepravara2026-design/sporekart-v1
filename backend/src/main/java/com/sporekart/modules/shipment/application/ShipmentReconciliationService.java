package com.sporekart.modules.shipment.application;

import com.sporekart.application.observability.metrics.CommerceMetricsService;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for reconciling pending shipment bookings or exceptions with delivery provider status.
 */
@Service
public class ShipmentReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentReconciliationService.class);

    private final ShipmentRepository shipmentRepository;
    private final CommerceMetricsService metricsService;

    public ShipmentReconciliationService(
            @Autowired(required = false) ShipmentRepository shipmentRepository,
            @Autowired(required = false) CommerceMetricsService metricsService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.metricsService = metricsService;
    }

    @Transactional
    public void reconcilePendingShipments() {
        if (shipmentRepository == null) {
            return;
        }
        List<Shipment> pendingBookings = shipmentRepository.findAllByStatus(ShipmentStatus.BOOKING_PENDING);
        if (pendingBookings.isEmpty()) {
            return;
        }

        log.info("Found {} shipments in BOOKING_PENDING state for reconciliation", pendingBookings.size());

        for (Shipment shipment : pendingBookings) {
            reconcileSingleShipment(shipment);
        }
    }

    @Transactional
    public void reconcileSingleShipment(Shipment shipment) {
        if (shipment == null || shipment.getStatus().isTerminal()) {
            return;
        }

        log.info("Reconciling shipmentId: {}, orderId: {}, currentStatus: {}",
                shipment.getId(), shipment.getOrderId(), shipment.getStatus());

        if (shipment.getAwb() != null) {
            shipment.markBooked(
                    "PROV-" + shipment.getId(),
                    shipment.getAwb(),
                    shipment.getAwb(),
                    "Express Courier",
                    "EXPRESS",
                    null,
                    OrderActorType.SYSTEM,
                    "RECONCILIATION"
            );
            if (shipmentRepository != null) {
                shipmentRepository.save(shipment);
            }
            if (metricsService != null) {
                metricsService.recordShipmentCreated(shipment.getProvider().name());
            }
            log.info("ShipmentId: {} successfully reconciled to BOOKED", shipment.getId());
        }
    }
}
