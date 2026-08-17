package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.ProviderFailureCategory;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.provider.NotificationProviderResult;
import com.sporekart.modules.notification.infrastructure.resilience.NotificationCircuitBreaker;
import com.sporekart.modules.notification.infrastructure.resilience.ProviderHealthTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public class NotificationResilienceService {

    private static final Logger log = LoggerFactory.getLogger(NotificationResilienceService.class);

    private final NotificationCircuitBreaker circuitBreaker;
    private final ProviderHealthTracker healthTracker;
    private final NotificationProperties properties;
    private final Random random = new Random();

    public NotificationResilienceService(NotificationCircuitBreaker circuitBreaker,
                                         ProviderHealthTracker healthTracker,
                                         NotificationProperties properties) {
        this.circuitBreaker = circuitBreaker;
        this.healthTracker = healthTracker;
        this.properties = properties;
    }

    public boolean allowExecution(String providerName, NotificationChannel channel) {
        if (!properties.getResilience().isCircuitBreakerEnabled()) {
            return true;
        }
        return circuitBreaker.allowExecution(providerName, channel);
    }

    public NotificationProviderResult handleCircuitOpen(String providerName, NotificationChannel channel) {
        log.warn("Circuit Breaker OPEN for provider '{}' channel '{}': suppressing outbound call", providerName, channel);
        healthTracker.recordFailure(providerName, channel, ProviderFailureCategory.TRANSIENT, 0, true);
        return NotificationProviderResult.failure("Circuit breaker is OPEN for " + providerName, true);
    }

    public void recordSuccess(String providerName, NotificationChannel channel, long durationMs) {
        circuitBreaker.recordSuccess(providerName, channel);
        healthTracker.recordSuccess(providerName, channel, durationMs);
    }

    public void recordFailure(String providerName, NotificationChannel channel, String errorMessage, boolean transientFailure, long durationMs) {
        ProviderFailureCategory category = classifyFailure(errorMessage, transientFailure);

        if (category == ProviderFailureCategory.AUTHENTICATION || category == ProviderFailureCategory.CONFIGURATION) {
            log.error("Permanent authentication/configuration error for provider '{}': {}", providerName, errorMessage);
        }

        circuitBreaker.recordFailure(providerName, channel);
        healthTracker.recordFailure(providerName, channel, category, durationMs, circuitBreaker.getState(providerName, channel) == NotificationCircuitBreaker.State.OPEN);
    }

    public ProviderFailureCategory classifyFailure(String errorMessage, boolean isTransient) {
        if (errorMessage == null) return ProviderFailureCategory.UNKNOWN;
        String clean = errorMessage.toLowerCase();

        if (clean.contains("401") || clean.contains("403") || clean.contains("unauthorized") ||
            clean.contains("invalid api key") || clean.contains("missing") || clean.contains("auth") || clean.contains("credentials")) {
            return ProviderFailureCategory.AUTHENTICATION;
        }
        if (clean.contains("429") || clean.contains("rate limit") || clean.contains("too many requests")) {
            return ProviderFailureCategory.RATE_LIMITED;
        }
        if (clean.contains("timeout") || clean.contains("timed out") || clean.contains("connecttimeout") || clean.contains("readtimeout")) {
            return ProviderFailureCategory.TIMEOUT;
        }
        if (!isTransient) {
            return ProviderFailureCategory.PERMANENT;
        }
        return ProviderFailureCategory.TRANSIENT;
    }

    public Instant calculateNextRetrySchedule(int attemptCount, String errorMessage) {
        long baseDelayMs = properties.getResilience().getBaseRetryDelayMs();
        long maxDelayMs = properties.getResilience().getMaxRetryDelayMs();
        long jitterMaxMs = properties.getResilience().getJitterMaxMs();

        long exponentialMs = baseDelayMs * (long) Math.pow(2, Math.max(0, attemptCount - 1));
        long boundedDelay = Math.min(maxDelayMs, exponentialMs);

        // Add random jitter to prevent retry storms
        long jitter = jitterMaxMs > 0 ? (long) (random.nextDouble() * jitterMaxMs) : 0;
        long totalDelayMs = boundedDelay + jitter;

        return Instant.now().plusMillis(totalDelayMs);
    }

    public NotificationCircuitBreaker getCircuitBreaker() { return circuitBreaker; }
    public ProviderHealthTracker getHealthTracker() { return healthTracker; }
}
