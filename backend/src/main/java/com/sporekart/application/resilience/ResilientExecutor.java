package com.sporekart.application.resilience;

import com.sporekart.application.observability.metrics.CommerceMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reusable execution engine performing controlled exponential backoff retries with jitter
 * for operations classified as transient.
 */
@Component
public class ResilientExecutor {

    private static final Logger log = LoggerFactory.getLogger(ResilientExecutor.class);

    private final CommerceMetricsService metricsService;

    public ResilientExecutor(@Autowired(required = false) CommerceMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public <T> T executeWithRetry(String operationName, ResilienceProperties.Policy policy, Callable<T> task) {
        int maxAttempts = policy != null ? Math.max(1, policy.getMaxAttempts()) : 3;
        long backoffMs = policy != null ? policy.getInitialBackoffMs() : 200;
        double multiplier = policy != null ? policy.getBackoffMultiplier() : 2.0;
        double jitter = policy != null ? policy.getJitterFactor() : 0.25;

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return task.call();
            } catch (Exception ex) {
                lastException = ex;

                if (!isRetryable(ex) || attempt == maxAttempts) {
                    log.error("Operation '{}' failed on attempt {}/{} (non-retryable or max attempts reached): {}",
                            operationName, attempt, maxAttempts, ex.getMessage());
                    break;
                }

                long calculatedDelay = (long) (backoffMs * Math.pow(multiplier, attempt - 1));
                long jitterAmount = (long) (calculatedDelay * jitter * (ThreadLocalRandom.current().nextDouble() * 2 - 1));
                long sleepMs = Math.max(10, calculatedDelay + jitterAmount);

                log.warn("Operation '{}' failed on attempt {}/{} ({}), retrying in {}ms...",
                        operationName, attempt, maxAttempts, ex.getMessage(), sleepMs);

                if (metricsService != null) {
                    metricsService.recordRateLimitRejected("RETRY_" + operationName.toUpperCase());
                }

                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during resilience retry sleep", ie);
                }
            }
        }

        if (lastException instanceof RuntimeException runtimeEx) {
            throw runtimeEx;
        }
        throw new RuntimeException("Resilient execution failed for operation: " + operationName, lastException);
    }

    public boolean isRetryable(Throwable ex) {
        if (ex == null) return false;

        if (ex instanceof TransientFailureException ||
            ex instanceof SocketTimeoutException ||
            ex instanceof ConnectException) {
            return true;
        }

        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("timeout") || msg.contains("connection reset") || msg.contains("502") || msg.contains("503") || msg.contains("504")) {
            return true;
        }

        if (ex.getCause() != null && ex.getCause() != ex) {
            return isRetryable(ex.getCause());
        }

        return false;
    }
}
