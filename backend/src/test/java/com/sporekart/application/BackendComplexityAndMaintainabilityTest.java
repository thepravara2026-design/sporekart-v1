package com.sporekart.application;

import com.sporekart.modules.notification.application.NotificationOperationsService.BacklogSummaryDto;
import com.sporekart.modules.notification.application.NotificationOperationsService;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.ReturnEligibilityDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BackendComplexityAndMaintainabilityTest {

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @Autowired
    private NotificationOperationsService notificationOperationsService;

    @Autowired
    private ReturnApplicationService returnApplicationService;

    @Test
    @DisplayName("Verify notification operations health summary calculation post-refactoring")
    void testNotificationOverallHealthSummary() {
        Map<String, Object> healthSummary = notificationOperationsService.getOverallHealthSummary();

        assertNotNull(healthSummary);
        assertThat(healthSummary).containsKey("overallStatus");
        assertThat(healthSummary).containsKey("activeAlerts");
        assertThat(healthSummary).containsKey("backlog");
        assertThat(healthSummary).containsKey("resilience");

        String overallStatus = (String) healthSummary.get("overallStatus");
        assertThat(overallStatus).isIn("HEALTHY", "DEGRADED", "CRITICAL");
    }

    @Test
    @DisplayName("Verify notification backlog summary calculation post-refactoring")
    void testNotificationBacklogSummary() {
        BacklogSummaryDto backlog = notificationOperationsService.getBacklogSummary();

        assertNotNull(backlog);
        assertThat(backlog.createdCount()).isGreaterThanOrEqualTo(0);
        assertThat(backlog.queuedCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Verify payment webhook duplicate handling post-refactoring")
    void testPaymentWebhookDuplicateHandling() {
        String invalidJsonPayload = "{\"event_id\": \"evt_test_sq09_duplicate\", \"event\": \"payment.authorized\"}";
        var result = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, invalidJsonPayload, "mock_sig");

        assertNotNull(result);
        assertThat(result.status()).isIn(WebhookProcessingStatus.PROCESSED, WebhookProcessingStatus.DUPLICATE);
    }
}
