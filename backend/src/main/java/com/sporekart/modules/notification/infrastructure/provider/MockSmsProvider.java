package com.sporekart.modules.notification.infrastructure.provider;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class MockSmsProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSmsProvider.class);

    public enum SimulationMode {
        SUCCESS,
        TRANSIENT_FAILURE,
        PERMANENT_FAILURE,
        TIMEOUT
    }

    private final ConcurrentMap<String, SimulationMode> recipientOverrides = new ConcurrentHashMap<>();
    private volatile SimulationMode defaultMode = SimulationMode.SUCCESS;

    public void setDefaultMode(SimulationMode mode) {
        this.defaultMode = mode;
    }

    public void setRecipientMode(String recipient, SimulationMode mode) {
        this.recipientOverrides.put(recipient, mode);
    }

    public void clearOverrides() {
        this.recipientOverrides.clear();
        this.defaultMode = SimulationMode.SUCCESS;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }

    @Override
    public String getProviderName() {
        return "MockSmsProvider";
    }

    @Override
    public NotificationProviderResult send(Notification notification) {
        SimulationMode mode = recipientOverrides.getOrDefault(notification.getRecipient(), defaultMode);
        log.info("Sending SMS to '{}' [Mode: {}]", notification.getRecipient(), mode);

        return switch (mode) {
            case SUCCESS -> NotificationProviderResult.success("SMS-MSG-" + UUID.randomUUID().toString().substring(0, 8));
            case TRANSIENT_FAILURE -> NotificationProviderResult.failure("SMS Gateway Rate Limit Exceeded: 429", true);
            case PERMANENT_FAILURE -> NotificationProviderResult.failure("Invalid phone number format: +0000000", false);
            case TIMEOUT -> NotificationProviderResult.failure("SMS Gateway Timeout", true);
        };
    }
}
