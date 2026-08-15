package com.sporekart.application.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Service capturing business and operational domain metrics with low-cardinality tags.
 */
@Service
public class CommerceMetricsService {

    private final MeterRegistry meterRegistry;

    public CommerceMetricsService(@org.springframework.beans.factory.annotation.Autowired(required = false) MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry != null ? meterRegistry : new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
    }

    public void recordOrderCreated(String channel) {
        Counter.builder("sporekart.orders.created")
                .description("Total orders created")
                .tag("channel", channel != null ? channel : "web")
                .register(meterRegistry)
                .increment();
    }

    public void recordOrderCancelled(String reason) {
        Counter.builder("sporekart.orders.cancelled")
                .description("Total orders cancelled")
                .tag("reason", reason != null ? reason : "customer_cancelled")
                .register(meterRegistry)
                .increment();
    }

    public void recordOrderCompleted() {
        Counter.builder("sporekart.orders.completed")
                .description("Total orders successfully completed and delivered")
                .register(meterRegistry)
                .increment();
    }

    public void recordOrderFailed(String reason) {
        Counter.builder("sporekart.orders.failed")
                .description("Total order processing failures")
                .tag("reason", reason != null ? reason : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentAttempt(String provider) {
        Counter.builder("sporekart.payments.attempted")
                .description("Total payment attempts")
                .tag("provider", provider != null ? provider : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentSuccess(String provider) {
        Counter.builder("sporekart.payments.succeeded")
                .description("Total successful payments")
                .tag("provider", provider != null ? provider : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentFailure(String provider, String reason) {
        Counter.builder("sporekart.payments.failed")
                .description("Total failed payment attempts")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("reason", reason != null ? reason : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordRefund(String provider, boolean success) {
        Counter.builder("sporekart.payments.refunds")
                .description("Total refund transactions")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
    }

    public void recordShipmentCreated(String provider) {
        Counter.builder("sporekart.shipments.created")
                .description("Total shipments created")
                .tag("provider", provider != null ? provider : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordShipmentFailed(String provider) {
        Counter.builder("sporekart.shipments.failed")
                .description("Total shipment creation failures")
                .tag("provider", provider != null ? provider : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordShipmentDelivered(String provider) {
        Counter.builder("sporekart.shipments.delivered")
                .description("Total shipments marked as delivered")
                .tag("provider", provider != null ? provider : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordInventoryReservation(boolean success) {
        Counter.builder("sporekart.inventory.reservations")
                .description("Total stock reservations attempted")
                .tag("status", success ? "success" : "insufficient_stock")
                .register(meterRegistry)
                .increment();
    }

    public void recordAuthLogin(boolean success) {
        Counter.builder("sporekart.auth.logins")
                .description("Total authentication login attempts")
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
    }

    public void recordAuthLockout() {
        Counter.builder("sporekart.auth.lockouts")
                .description("Total account lockouts triggered")
                .register(meterRegistry)
                .increment();
    }

    public void recordRateLimitRejected(String category) {
        Counter.builder("sporekart.ratelimit.rejected")
                .description("Total rate limit breaches rejected")
                .tag("category", category != null ? category : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordIdempotencyReplayed() {
        Counter.builder("sporekart.idempotency.replayed")
                .description("Total idempotent requests replayed")
                .register(meterRegistry)
                .increment();
    }

    public void recordIdempotencyConflict() {
        Counter.builder("sporekart.idempotency.conflicts")
                .description("Total idempotency payload conflicts")
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookReceived(String provider, String eventType) {
        Counter.builder("sporekart.webhooks.received")
                .description("Total webhooks received")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("eventType", eventType != null ? eventType : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookDuplicate(String provider) {
        Counter.builder("sporekart.webhooks.duplicates")
                .description("Total duplicate webhooks skipped")
                .tag("provider", provider != null ? provider : "unknown")
                .register(meterRegistry)
                .increment();
    }
}
