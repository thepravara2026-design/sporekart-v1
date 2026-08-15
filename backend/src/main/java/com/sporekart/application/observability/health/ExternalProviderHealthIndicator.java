package com.sporekart.application.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for external provider integration status (Razorpay, Shipping).
 * Degraded status on third-party provider issues reports DEGRADED rather than terminating liveness.
 */
@Component("externalProviderHealthIndicator")
public class ExternalProviderHealthIndicator implements HealthIndicator {

    private volatile boolean razorpayHealthy = true;
    private volatile boolean shippingHealthy = true;

    @Override
    public Health health() {
        if (razorpayHealthy && shippingHealthy) {
            return Health.up()
                    .withDetail("razorpay", "OPERATIONAL")
                    .withDetail("shippingProvider", "OPERATIONAL")
                    .build();
        }

        return Health.status("DEGRADED")
                .withDetail("razorpay", razorpayHealthy ? "OPERATIONAL" : "DEGRADED_UNAVAILABLE")
                .withDetail("shippingProvider", shippingHealthy ? "OPERATIONAL" : "DEGRADED_UNAVAILABLE")
                .build();
    }

    public void setRazorpayHealthy(boolean razorpayHealthy) {
        this.razorpayHealthy = razorpayHealthy;
    }

    public void setShippingHealthy(boolean shippingHealthy) {
        this.shippingHealthy = shippingHealthy;
    }
}
