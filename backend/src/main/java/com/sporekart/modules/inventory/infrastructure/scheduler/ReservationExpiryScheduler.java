package com.sporekart.modules.inventory.infrastructure.scheduler;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final InventoryApplicationService inventoryApplicationService;

    public ReservationExpiryScheduler(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @Scheduled(fixedDelayString = "${sporekart.inventory.expiry-interval:60000}")
    public void cleanupExpiredReservations() {
        try {
            int expiredCount = inventoryApplicationService.expireReservationsBatch();
            if (expiredCount > 0) {
                log.info("ReservationExpiryScheduler: successfully expired and released {} reservations", expiredCount);
            }
        } catch (Exception e) {
            log.error("ReservationExpiryScheduler: error during reservation expiry batch cleanup: {}", e.getMessage(), e);
        }
    }
}
