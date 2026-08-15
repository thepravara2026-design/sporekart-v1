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
public class MockWhatsAppProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockWhatsAppProvider.class);

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
        return channel == NotificationChannel.WHATSAPP;
    }

    @Override
    public String getProviderName() {
        return "MockWhatsAppProvider";
    }

    @Override
    public NotificationProviderResult send(Notification notification) {
        SimulationMode mode = recipientOverrides.getOrDefault(notification.getRecipient(), defaultMode);
        log.info("Sending WHATSAPP message to '{}' [Mode: {}]", notification.getRecipient(), mode);

        return switch (mode) {
            case SUCCESS -> NotificationProviderResult.success("WA-MSG-" + UUID.randomUUID().toString().substring(0, 8));
            case TRANSIENT_FAILURE -> NotificationProviderResult.failure("WhatsApp Business API Temporary Downtime: 502", true);
            case PERMANENT_FAILURE -> NotificationProviderResult.failure("Recipient phone number not registered on WhatsApp", false);
            case TIMEOUT -> NotificationProviderResult.failure("WhatsApp Webhook Response Timeout", true);
        };
    }
}
