package com.sporekart.modules.notification.domain;

import com.sporekart.modules.notification.application.TemplatePlaceholderSubstitutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification Domain & State Machine Unit Tests")
public class NotificationDomainTest {

    @Nested
    @DisplayName("State Machine Transition Rules")
    class StateMachineTests {

        @Test
        @DisplayName("Valid transition CREATED -> PROCESSING -> SENT -> DELIVERED should succeed")
        void validTransitionSequenceShouldSucceed() {
            Notification notification = new Notification(
                    "evt-123", "ORDER_CREATED", "user-1", "cust-1",
                    NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                    "customer@example.com", "Order Confirmation", "Thank you for order",
                    NotificationPriority.NORMAL, "idem-123", "corr-123", "trace-123"
            );

            assertEquals(NotificationStatus.CREATED, notification.getStatus());

            notification.markProcessing("MockEmailProvider");
            assertEquals(NotificationStatus.PROCESSING, notification.getStatus());
            assertEquals("MockEmailProvider", notification.getProviderName());
            assertEquals(1, notification.getAttemptCount());

            notification.markSent("MSG-001");
            assertEquals(NotificationStatus.SENT, notification.getStatus());
            assertEquals("MSG-001", notification.getProviderMessageId());

            notification.markDelivered("MSG-001");
            assertEquals(NotificationStatus.DELIVERED, notification.getStatus());
            assertNotNull(notification.getDeliveredAt());
        }

        @Test
        @DisplayName("Invalid state transition DELIVERED -> CREATED should throw IllegalStateException")
        void invalidStateTransitionShouldThrowException() {
            Notification notification = new Notification(
                    "evt-123", "ORDER_CREATED", "user-1", "cust-1",
                    NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                    "customer@example.com", "Order Confirmation", "Thank you for order",
                    NotificationPriority.NORMAL, "idem-123", "corr-123", "trace-123"
            );

            notification.markProcessing("MockEmailProvider");
            notification.markSent("MSG-001");
            notification.markDelivered("MSG-001");

            assertThrows(IllegalStateException.class, () -> notification.transitionTo(NotificationStatus.CREATED));
        }

        @Test
        @DisplayName("Transient failure should transition to RETRY_SCHEDULED")
        void transientFailureShouldScheduleRetry() {
            Notification notification = new Notification(
                    "evt-123", "ORDER_CREATED", "user-1", "cust-1",
                    NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                    "customer@example.com", "Order Confirmation", "Thank you for order",
                    NotificationPriority.NORMAL, "idem-123", "corr-123", "trace-123"
            );

            notification.markProcessing("MockEmailProvider");
            notification.markFailed("Connection timed out", false);

            assertEquals(NotificationStatus.RETRY_SCHEDULED, notification.getStatus());
            assertNotNull(notification.getFailedAt());
            assertEquals("Connection timed out", notification.getFailureReason());
        }

        @Test
        @DisplayName("Permanent failure should transition to FAILED_PERMANENTLY")
        void permanentFailureShouldFailPermanently() {
            Notification notification = new Notification(
                    "evt-123", "ORDER_CREATED", "user-1", "cust-1",
                    NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                    "customer@example.com", "Order Confirmation", "Thank you for order",
                    NotificationPriority.NORMAL, "idem-123", "corr-123", "trace-123"
            );

            notification.markProcessing("MockEmailProvider");
            notification.markFailed("User email address rejected", true);

            assertEquals(NotificationStatus.FAILED_PERMANENTLY, notification.getStatus());
        }
    }

    @Nested
    @DisplayName("Notification Category & Preference Logic")
    class CategoryPreferenceTests {

        @Test
        @DisplayName("Mandatory categories (PAYMENT, SECURITY, SYSTEM_ALERT) cannot be disabled")
        void mandatoryCategoriesCannotBeDisabled() {
            assertTrue(NotificationCategory.PAYMENT.isMandatory());
            assertTrue(NotificationCategory.SECURITY.isMandatory());
            assertTrue(NotificationCategory.SYSTEM_ALERT.isMandatory());
            assertFalse(NotificationCategory.ORDER_UPDATES.isMandatory());
            assertFalse(NotificationCategory.PROMOTIONAL.isMandatory());

            NotificationPreference pref = new NotificationPreference("user-1", NotificationCategory.PAYMENT, false, false, false, false);
            assertTrue(pref.isChannelEnabled(NotificationChannel.EMAIL));
            assertTrue(pref.isChannelEnabled(NotificationChannel.SMS));
        }

        @Test
        @DisplayName("Non-mandatory categories honor opt-out settings")
        void nonMandatoryCategoriesHonorOptOut() {
            NotificationPreference pref = new NotificationPreference("user-1", NotificationCategory.PROMOTIONAL, true, false, true, false);
            assertTrue(pref.isChannelEnabled(NotificationChannel.EMAIL));
            assertFalse(pref.isChannelEnabled(NotificationChannel.SMS));
            assertTrue(pref.isChannelEnabled(NotificationChannel.WHATSAPP));
            assertFalse(pref.isChannelEnabled(NotificationChannel.IN_APP));
        }
    }

    @Nested
    @DisplayName("Template Placeholder Substitution")
    class PlaceholderSubstitutionTests {

        private final TemplatePlaceholderSubstitutor substitutor = new TemplatePlaceholderSubstitutor();

        @Test
        @DisplayName("Placeholder substitution replaces variables and sanitizes HTML tags")
        void placeholderSubstitutionReplacesVariablesAndSanitizesHtml() {
            String template = "Hello {customerName}, your order {orderNumber} for total {orderTotal} is confirmed!";
            Map<String, Object> vars = Map.of(
                    "customerName", "Alice <script>alert(1)</script>",
                    "orderNumber", "ORD-998877",
                    "orderTotal", "$124.50"
            );

            String result = substitutor.substitute(template, vars);

            assertTrue(result.contains("Alice &lt;script&gt;alert(1)&lt;/script&gt;"));
            assertTrue(result.contains("ORD-998877"));
            assertTrue(result.contains("$124.50"));
            assertFalse(result.contains("<script>"));
        }
    }
}
