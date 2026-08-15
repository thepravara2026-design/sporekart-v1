package com.sporekart.modules.shipment.infrastructure.scheduler;

import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ShipmentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShipmentReconciliationScheduler.class);

    private final ShipmentApplicationService shipmentApplicationService;

    public ShipmentReconciliationScheduler(ShipmentApplicationService shipmentApplicationService) {
        this.shipmentApplicationService = shipmentApplicationService;
    }

    @Scheduled(cron = "${sporekart.shipping.reconciliation-cron:0 */5 * * * *}")
    public void runReconciliation() {
        log.info("Running background shipment status reconciliation...");
        try {
            shipmentApplicationService.reconcileActiveShipments();
        } catch (Exception e) {
            log.error("Error during shipment reconciliation run: {}", e.getMessage(), e);
        }
    }
}
