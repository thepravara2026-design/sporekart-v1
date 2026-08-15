package com.sporekart.modules.inventory.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "sporekart.inventory")
public class InventoryProperties {

    private Duration reservationTtl = Duration.ofMinutes(15);
    private int expiryBatchSize = 50;

    public Duration getReservationTtl() {
        return reservationTtl;
    }

    public void setReservationTtl(Duration reservationTtl) {
        this.reservationTtl = reservationTtl;
    }

    public int getExpiryBatchSize() {
        return expiryBatchSize;
    }

    public void setExpiryBatchSize(int expiryBatchSize) {
        this.expiryBatchSize = expiryBatchSize;
    }
}
