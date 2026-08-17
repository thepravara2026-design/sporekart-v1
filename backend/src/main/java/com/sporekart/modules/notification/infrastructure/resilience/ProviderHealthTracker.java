package com.sporekart.modules.notification.infrastructure.resilience;

import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.ProviderFailureCategory;
import com.sporekart.modules.notification.domain.ProviderHealthStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProviderHealthTracker {

    public static class ProviderHealthDetail {
        private final String providerName;
        private final NotificationChannel channel;
        private ProviderHealthStatus status;
        private int consecutiveSuccesses;
        private int consecutiveFailures;
        private Instant lastSuccessAt;
        private Instant lastFailureAt;
        private ProviderFailureCategory lastFailureCategory;
        private long lastLatencyMs;

        public ProviderHealthDetail(String providerName, NotificationChannel channel) {
            this.providerName = providerName;
            this.channel = channel;
            this.status = ProviderHealthStatus.HEALTHY;
        }

        public synchronized void recordSuccess(long latencyMs) {
            this.consecutiveSuccesses++;
            this.consecutiveFailures = 0;
            this.lastSuccessAt = Instant.now();
            this.lastLatencyMs = latencyMs;
            this.status = ProviderHealthStatus.HEALTHY;
        }

        public synchronized void recordFailure(ProviderFailureCategory category, long latencyMs, boolean circuitOpen) {
            this.consecutiveFailures++;
            this.consecutiveSuccesses = 0;
            this.lastFailureAt = Instant.now();
            this.lastFailureCategory = category;
            this.lastLatencyMs = latencyMs;

            if (category == ProviderFailureCategory.AUTHENTICATION || category == ProviderFailureCategory.CONFIGURATION || circuitOpen) {
                this.status = ProviderHealthStatus.UNAVAILABLE;
            } else if (consecutiveFailures >= 2) {
                this.status = ProviderHealthStatus.DEGRADED;
            }
        }

        public String getProviderName() { return providerName; }
        public NotificationChannel getChannel() { return channel; }
        public ProviderHealthStatus getStatus() { return status; }
        public int getConsecutiveSuccesses() { return consecutiveSuccesses; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
        public Instant getLastSuccessAt() { return lastSuccessAt; }
        public Instant getLastFailureAt() { return lastFailureAt; }
        public ProviderFailureCategory getLastFailureCategory() { return lastFailureCategory; }
        public long getLastLatencyMs() { return lastLatencyMs; }
    }

    private final Map<String, ProviderHealthDetail> healthMap = new ConcurrentHashMap<>();

    public ProviderHealthDetail getHealth(String providerName, NotificationChannel channel) {
        String key = buildKey(providerName, channel);
        return healthMap.computeIfAbsent(key, k -> new ProviderHealthDetail(providerName, channel));
    }

    public void recordSuccess(String providerName, NotificationChannel channel, long latencyMs) {
        getHealth(providerName, channel).recordSuccess(latencyMs);
    }

    public void recordFailure(String providerName, NotificationChannel channel, ProviderFailureCategory category, long latencyMs, boolean circuitOpen) {
        getHealth(providerName, channel).recordFailure(category, latencyMs, circuitOpen);
    }

    public Map<String, Object> getHealthSummary(NotificationCircuitBreaker circuitBreaker) {
        Map<String, Object> summary = new ConcurrentHashMap<>();
        healthMap.forEach((key, detail) -> {
            Map<String, Object> info = new ConcurrentHashMap<>();
            info.put("status", detail.getStatus().name());
            info.put("channel", detail.getChannel() != null ? detail.getChannel().name() : "UNKNOWN");
            info.put("consecutiveSuccesses", detail.getConsecutiveSuccesses());
            info.put("consecutiveFailures", detail.getConsecutiveFailures());
            info.put("lastSuccessAt", detail.getLastSuccessAt() != null ? detail.getLastSuccessAt().toString() : "NEVER");
            info.put("lastFailureAt", detail.getLastFailureAt() != null ? detail.getLastFailureAt().toString() : "NEVER");
            info.put("lastFailureCategory", detail.getLastFailureCategory() != null ? detail.getLastFailureCategory().name() : "NONE");
            info.put("lastLatencyMs", detail.getLastLatencyMs());
            if (circuitBreaker != null && detail.getChannel() != null) {
                info.put("circuitState", circuitBreaker.getState(detail.getProviderName(), detail.getChannel()).name());
            }
            summary.put(key, info);
        });
        return summary;
    }

    private String buildKey(String providerName, NotificationChannel channel) {
        String prov = providerName != null ? providerName.toUpperCase() : "UNKNOWN";
        String chan = channel != null ? channel.name() : "ALL";
        return prov + ":" + chan;
    }
}
