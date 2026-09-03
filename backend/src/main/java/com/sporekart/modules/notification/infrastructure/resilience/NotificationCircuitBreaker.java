package com.sporekart.modules.notification.infrastructure.resilience;

import com.sporekart.modules.notification.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(NotificationCircuitBreaker.class);

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    public static class CircuitConfig {
        private int failureThreshold = 3;
        private long openDurationMs = 10000; // 10 seconds cooldown
        private int halfOpenProbeCount = 1;

        public CircuitConfig() {}

        public CircuitConfig(int failureThreshold, long openDurationMs, int halfOpenProbeCount) {
            this.failureThreshold = failureThreshold;
            this.openDurationMs = openDurationMs;
            this.halfOpenProbeCount = halfOpenProbeCount;
        }

        public int getFailureThreshold() { return failureThreshold; }
        public long getOpenDurationMs() { return openDurationMs; }
        public int getHalfOpenProbeCount() { return halfOpenProbeCount; }
    }

    private final Map<String, CircuitState> circuits = new ConcurrentHashMap<>();
    private CircuitConfig defaultConfig = new CircuitConfig();

    public void setConfig(CircuitConfig config) {
        if (config != null) {
            this.defaultConfig = config;
        }
    }

    public boolean allowExecution(String providerName, NotificationChannel channel) {
        String key = buildKey(providerName, channel);
        CircuitState state = circuits.computeIfAbsent(key, k -> new CircuitState());
        return state.allowExecution(defaultConfig);
    }

    public void recordSuccess(String providerName, NotificationChannel channel) {
        String key = buildKey(providerName, channel);
        CircuitState state = circuits.get(key);
        if (state != null) {
            state.recordSuccess();
        }
    }

    public void recordFailure(String providerName, NotificationChannel channel) {
        String key = buildKey(providerName, channel);
        CircuitState state = circuits.computeIfAbsent(key, k -> new CircuitState());
        state.recordFailure(defaultConfig);
    }

    public State getState(String providerName, NotificationChannel channel) {
        String key = buildKey(providerName, channel);
        CircuitState state = circuits.get(key);
        if (state == null) {
            return State.CLOSED;
        }
        return state.getState(defaultConfig);
    }

    public void reset(String providerName, NotificationChannel channel) {
        String key = buildKey(providerName, channel);
        circuits.remove(key);
    }

    private String buildKey(String providerName, NotificationChannel channel) {
        String prov = providerName != null ? providerName.toUpperCase() : "UNKNOWN";
        String chan = channel != null ? channel.name() : "ALL";
        return prov + ":" + chan;
    }

    private static class CircuitState {
        private State state = State.CLOSED;
        private int consecutiveFailures = 0;
        private int halfOpenProbesSent = 0;
        private Instant lastStateChange = Instant.now();

        public synchronized boolean allowExecution(CircuitConfig config) {
            Instant now = Instant.now();

            if (state == State.OPEN) {
                long elapsedMs = now.toEpochMilli() - lastStateChange.toEpochMilli();
                if (elapsedMs >= config.getOpenDurationMs()) {
                    state = State.HALF_OPEN;
                    halfOpenProbesSent = 1;
                    lastStateChange = now;
                    log.info("Circuit transition to HALF_OPEN after cooldown elapsed ({} ms)", elapsedMs);
                    return true;
                }
                return false;
            }

            if (state == State.HALF_OPEN) {
                if (halfOpenProbesSent < config.getHalfOpenProbeCount()) {
                    halfOpenProbesSent++;
                    return true;
                }
                return false;
            }

            return true; // CLOSED
        }

        public synchronized void recordSuccess() {
            if (state == State.HALF_OPEN || state == State.OPEN) {
                log.info("Circuit state recovered: transitioning to CLOSED");
            }
            state = State.CLOSED;
            consecutiveFailures = 0;
            halfOpenProbesSent = 0;
            lastStateChange = Instant.now();
        }

        public synchronized void recordFailure(CircuitConfig config) {
            consecutiveFailures++;
            if (state == State.HALF_OPEN) {
                state = State.OPEN;
                lastStateChange = Instant.now();
                log.warn("Half-open probe failed: returning circuit state to OPEN");
            } else if (state == State.CLOSED && consecutiveFailures >= config.getFailureThreshold()) {
                state = State.OPEN;
                lastStateChange = Instant.now();
                log.warn("Failure threshold reached ({}/{}): opening circuit", consecutiveFailures, config.getFailureThreshold());
            }
        }

        public synchronized State getState(CircuitConfig config) {
            if (state == State.OPEN) {
                long elapsedMs = Instant.now().toEpochMilli() - lastStateChange.toEpochMilli();
                if (elapsedMs >= config.getOpenDurationMs()) {
                    return State.HALF_OPEN;
                }
            }
            return state;
        }
    }
}
