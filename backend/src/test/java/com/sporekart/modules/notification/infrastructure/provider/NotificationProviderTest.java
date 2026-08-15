package com.sporekart.modules.notification.infrastructure.provider;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification Provider & Simulation Mode Unit Tests")
public class NotificationProviderTest {

    private MockEmailProvider emailProvider;
    private MockSmsProvider smsProvider;
    private MockWhatsAppProvider whatsAppProvider;
    private InAppNotificationProvider inAppProvider;

    @BeforeEach
    void setUp() {
        emailProvider = new MockEmailProvider();
        smsProvider = new MockSmsProvider();
        whatsAppProvider = new MockWhatsAppProvider();
        inAppProvider = new InAppNotificationProvider();
    }

    @Test
    @DisplayName("MockEmailProvider should handle SUCCESS, TRANSIENT_FAILURE, PERMANENT_FAILURE, TIMEOUT simulation modes")
    void emailProviderSimulationModes() {
        Notification notification = createTestNotification(NotificationChannel.EMAIL, "user@example.com");

        // 1. Success mode
        emailProvider.setDefaultMode(MockEmailProvider.SimulationMode.SUCCESS);
        NotificationProviderResult r1 = emailProvider.send(notification);
        assertTrue(r1.success());
        assertNotNull(r1.providerMessageId());

        // 2. Transient failure mode
        emailProvider.setDefaultMode(MockEmailProvider.SimulationMode.TRANSIENT_FAILURE);
        NotificationProviderResult r2 = emailProvider.send(notification);
        assertFalse(r2.success());
        assertTrue(r2.transientFailure());

        // 3. Permanent failure mode
        emailProvider.setDefaultMode(MockEmailProvider.SimulationMode.PERMANENT_FAILURE);
        NotificationProviderResult r3 = emailProvider.send(notification);
        assertFalse(r3.success());
        assertFalse(r3.transientFailure());

        // 4. Timeout mode
        emailProvider.setDefaultMode(MockEmailProvider.SimulationMode.TIMEOUT);
        NotificationProviderResult r4 = emailProvider.send(notification);
        assertFalse(r4.success());
        assertTrue(r4.transientFailure());
    }

    @Test
    @DisplayName("InAppNotificationProvider should deliver successfully")
    void inAppNotificationProviderDelivery() {
        Notification notification = createTestNotification(NotificationChannel.IN_APP, "user-123");
        assertTrue(inAppProvider.supports(NotificationChannel.IN_APP));
        NotificationProviderResult result = inAppProvider.send(notification);
        assertTrue(result.success());
        assertNotNull(result.providerMessageId());
    }

    private Notification createTestNotification(NotificationChannel channel, String recipient) {
        return new Notification(
                "evt-100", "TEST_EVENT", "user-123", "cust-123",
                channel, "TEST_TEMPLATE", 1, recipient, "Subject", "Body content",
                NotificationPriority.NORMAL, "idem-key-" + System.nanoTime(), "corr-100", "trace-100"
        );
    }
}
