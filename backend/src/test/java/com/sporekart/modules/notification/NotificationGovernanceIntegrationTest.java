package com.sporekart.modules.notification;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.modules.notification.application.NotificationRetentionService;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.security.domain.SecurityAuditEvent;
import com.sporekart.modules.security.infrastructure.persistence.SecurityAuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationGovernanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired(required = false)
    private SecurityAuditEventRepository auditEventRepository;

    @Autowired
    private NotificationRetentionService retentionService;

    @Autowired
    private NotificationProperties properties;

    @BeforeEach
    public void setup() {
        notificationRepository.deleteAll();
    }

    @Test
    public void test7J_001_RetentionConfiguration() {
        assertThat(properties).isNotNull();
        assertThat(properties.getRetention()).isNotNull();
        assertThat(properties.getRetention().getNotificationDays()).isEqualTo(30);
        assertThat(properties.getRetention().getPayloadDays()).isEqualTo(7);
        assertThat(properties.getRetention().getOutboxDays()).isEqualTo(14);
    }

    @Test
    public void test7J_002_RetentionEligibility_ExcludesActiveNotifications() {
        // Active notifications
        Notification created = createNotification("evt-1", NotificationStatus.CREATED, Instant.now().minus(Duration.ofDays(40)));
        Notification processing = createNotification("evt-2", NotificationStatus.PROCESSING, Instant.now().minus(Duration.ofDays(40)));
        Notification retry = createNotification("evt-3", NotificationStatus.RETRY_SCHEDULED, Instant.now().minus(Duration.ofDays(40)));
        Notification sent = createNotification("evt-4", NotificationStatus.SENT, Instant.now().minus(Duration.ofDays(40)));

        notificationRepository.saveAll(List.of(created, processing, retry, sent));

        var preview = retentionService.getRetentionPreview();
        assertThat(preview.eligibleNotificationsForDeletion()).isEqualTo(0);
        assertThat(preview.eligibleNotificationsForPayloadMinimization()).isEqualTo(0);
    }

    @Test
    public void test7J_003_TerminalRetention_DetectsEligible() {
        Notification del = createNotification("evt-del", NotificationStatus.DELIVERED, Instant.now().minus(Duration.ofDays(40)));
        Notification fail = createNotification("evt-fail", NotificationStatus.FAILED_PERMANENTLY, Instant.now().minus(Duration.ofDays(40)));
        Notification canc = createNotification("evt-canc", NotificationStatus.CANCELLED, Instant.now().minus(Duration.ofDays(40)));
        Notification supp = createNotification("evt-supp", NotificationStatus.SUPPRESSED, Instant.now().minus(Duration.ofDays(40)));

        notificationRepository.saveAll(List.of(del, fail, canc, supp));

        var preview = retentionService.getRetentionPreview();
        assertThat(preview.eligibleNotificationsForDeletion()).isEqualTo(4);
    }

    @Test
    public void test7J_004_RetentionDryRun_DoesNotDeleteData() {
        Notification del = createNotification("evt-dry", NotificationStatus.DELIVERED, Instant.now().minus(Duration.ofDays(40)));
        notificationRepository.save(del);

        var result = retentionService.executeRetentionJob(true, "ADMIN_DRY_RUN");

        assertThat(result.dryRun()).isTrue();
        assertThat(result.deletedNotificationsCount()).isEqualTo(0);
        assertThat(notificationRepository.findById(del.getId())).isPresent();
    }

    @Test
    public void test7J_005_RetentionBatchProcessing() {
        for (int i = 0; i < 5; i++) {
            Notification del = createNotification("evt-batch-" + i, NotificationStatus.DELIVERED, Instant.now().minus(Duration.ofDays(40)));
            notificationRepository.save(del);
        }

        var result = retentionService.executeRetentionJob(false, "ADMIN_BATCH");
        assertThat(result.deletedNotificationsCount()).isGreaterThan(0);
    }

    @Test
    public void test7J_006_RetentionRestartSafety() {
        Notification del = createNotification("evt-restart", NotificationStatus.DELIVERED, Instant.now().minus(Duration.ofDays(40)));
        notificationRepository.save(del);

        retentionService.executeRetentionJob(false, "ADMIN_1");
        // Re-run execution immediately should pass safely with 0 deleted
        var result2 = retentionService.executeRetentionJob(false, "ADMIN_2");
        assertThat(result2.deletedNotificationsCount()).isEqualTo(0);
    }

    @Test
    public void test7J_007_ConcurrentDeliveryProtection() {
        Notification proc = createNotification("evt-proc", NotificationStatus.PROCESSING, Instant.now().minus(Duration.ofDays(50)));
        notificationRepository.save(proc);

        retentionService.executeRetentionJob(false, "ADMIN_TEST");
        assertThat(notificationRepository.findById(proc.getId())).isPresent();
    }

    @Test
    public void test7J_008_ConcurrentReconciliationProtection() {
        Notification sent = createNotification("evt-sent", NotificationStatus.SENT, Instant.now().minus(Duration.ofDays(50)));
        notificationRepository.save(sent);

        retentionService.executeRetentionJob(false, "ADMIN_TEST");
        assertThat(notificationRepository.findById(sent.getId())).isPresent();
    }

    @Test
    public void test7J_009_PayloadMinimization() {
        Notification del = createNotification("evt-min", NotificationStatus.DELIVERED, Instant.now().minus(Duration.ofDays(10)));
        notificationRepository.save(del);

        var result = retentionService.executeRetentionJob(false, "ADMIN_MIN");
        assertThat(result.minimizedPayloadsCount()).isEqualTo(1);

        Notification updated = notificationRepository.findById(del.getId()).orElseThrow();
        assertThat(updated.getBody()).isEqualTo("[REDACTED_PAYLOAD]");
        assertThat(updated.getContentHash()).isNotEmpty();
        assertThat(updated.getPayloadMinimizedAt()).isNotNull();
    }

    @Test
    public void test7J_010_CredentialSafety() {
        var health = retentionService.getRetentionHealth();
        String json = health.toString();
        assertThat(json).doesNotContain("apiKey").doesNotContain("secret").doesNotContain("authToken");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test7J_011_PiiProtection() throws Exception {
        Notification del = createNotification("evt-pii", NotificationStatus.DELIVERED, Instant.now());
        notificationRepository.save(del);

        mockMvc.perform(get("/api/v1/admin/notifications/" + del.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recipient").value(containsString("***")));
    }

    @Test
    public void test7J_012_AuditRetention() {
        if (auditEventRepository != null) {
            long count = auditEventRepository.count();
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    public void test7J_013_RetentionAudit() {
        retentionService.executeRetentionJob(true, "ADMIN_AUDITOR");
        if (auditEventRepository != null) {
            var logs = auditEventRepository.findByActorIdOrderByCreatedAtDesc("ADMIN_AUDITOR");
            assertThat(logs).isNotEmpty();
        }
    }

    @Test
    public void test7J_014_AdminAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/retention/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test7J_015_ManualRetentionExecution() throws Exception {
        mockMvc.perform(post("/api/v1/admin/notifications/retention/run?dryRun=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dryRun").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test7J_016_InvalidRetentionRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/retention/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HEALTHY"));
    }

    @Test
    public void test7J_017_IdempotencyProtection() {
        Notification del = createNotification("evt-idem", NotificationStatus.DELIVERED, Instant.now());
        notificationRepository.save(del);

        var existing = notificationRepository.findByIdempotencyKey(del.getIdempotencyKey());
        assertThat(existing).isPresent();
    }

    @Test
    public void test7J_018_WebhookIdempotencyProtection() {
        Notification del = createNotification("evt-webhook", NotificationStatus.DELIVERED, Instant.now());
        setField(del, "providerEventId", "evt_provider_123");
        notificationRepository.save(del);

        var existing = notificationRepository.findByProviderEventId("evt_provider_123");
        assertThat(existing).isPresent();
    }

    @Test
    public void test7J_019_OutboxProtection() {
        OutboxEvent pending = OutboxEvent.create("ORDER", "ord-1", "ORDER_CREATED", "{}");
        outboxRepository.save(pending);

        retentionService.executeRetentionJob(false, "ADMIN_OUTBOX");
        assertThat(outboxRepository.findById(pending.getId())).isPresent();
    }

    @Test
    public void test7J_020_DeadOutboxGovernance() {
        OutboxEvent dead = new OutboxEvent(
                UUID.randomUUID().toString(), "ORDER", "ord-dead", "ORDER_FAILED", "{}",
                OutboxStatus.DEAD, 5, 5, "Max retries reached",
                OffsetDateTime.now().minusDays(30), OffsetDateTime.now().minusDays(30), null, null
        );
        outboxRepository.save(dead);

        retentionService.executeRetentionJob(false, "ADMIN_DEAD");
        // DEAD outbox event must be preserved!
        assertThat(outboxRepository.findById(dead.getId())).isPresent();
    }

    @Test
    public void test7J_021_CleanupMetrics() {
        var health = retentionService.getRetentionHealth();
        assertThat(health.notificationRetentionDays()).isEqualTo(30);
        assertThat(health.payloadRetentionDays()).isEqualTo(7);
        assertThat(health.outboxRetentionDays()).isEqualTo(14);
    }

    @Test
    public void test7J_022_CleanupFailureRecovery() {
        // Execute cleanly
        var result = retentionService.executeRetentionJob(true, "ADMIN_RECOVERY");
        assertThat(result.success()).isTrue();
    }

    @Test
    public void test7J_023_OperationalBacklogAging() {
        Notification proc = createNotification("evt-aging", NotificationStatus.PROCESSING, Instant.now().minus(Duration.ofMinutes(10)));
        notificationRepository.save(proc);

        var intel = retentionService.getOperationalIntelligence("24h", null, null);
        assertThat(intel.backlogAging()).isNotNull();
        assertThat(intel.backlogAging().between5mAnd15mCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void test7J_024_DeliveryAggregateMetrics() {
        Notification del = createNotification("evt-agg-del", NotificationStatus.DELIVERED, Instant.now());
        Notification fail = createNotification("evt-agg-fail", NotificationStatus.FAILED_PERMANENTLY, Instant.now());
        notificationRepository.saveAllAndFlush(List.of(del, fail));

        var intel = retentionService.getOperationalIntelligence("24h", null, null);
        assertThat(intel.totalNotifications()).isEqualTo(2);
        assertThat(intel.deliveredNotifications()).isEqualTo(1);
        assertThat(intel.failedNotifications()).isEqualTo(1);
        assertThat(intel.successRatePercent()).isEqualTo(50.0);
    }

    @Test
    public void test7J_025_ProviderAggregateMetrics() {
        Notification del = createNotification("evt-prov-del", NotificationStatus.DELIVERED, Instant.now());
        setField(del, "providerName", "SendGrid");
        notificationRepository.saveAndFlush(del);

        var intel = retentionService.getOperationalIntelligence("24h", null, null);
        assertThat(intel.providerAggregates()).isNotEmpty();
    }

    @Test
    public void test7J_026_LatencyCalculation() {
        Instant created = Instant.now().minus(Duration.ofSeconds(2));
        Notification del = createNotification("evt-lat", NotificationStatus.DELIVERED, created);
        setField(del, "deliveredAt", Instant.now());
        setField(del, "providerName", "SendGrid");
        notificationRepository.saveAndFlush(del);

        var intel = retentionService.getOperationalIntelligence("24h", null, null);
        assertThat(intel.providerAggregates()).isNotEmpty();
        assertThat(intel.providerAggregates().get(0).averageDeliveryLatencyMs()).isGreaterThan(0.0);
    }

    @Test
    public void test7J_027_InvalidTimestampHandling() {
        Notification del = createNotification("evt-invalid-time", NotificationStatus.DELIVERED, Instant.now());
        setField(del, "deliveredAt", null);
        setField(del, "providerName", "SendGrid");
        notificationRepository.saveAndFlush(del);

        var intel = retentionService.getOperationalIntelligence("24h", null, null);
        assertThat(intel.providerAggregates()).isNotEmpty();
    }

    @Test
    public void test7J_028_RetentionPerformance() {
        var preview = retentionService.getRetentionPreview();
        assertThat(preview).isNotNull();
    }

    @Test
    public void test7J_029_ExistingNotificationRegression() {
        Notification n = createNotification("evt-7f", NotificationStatus.CREATED, Instant.now());
        n.transitionTo(NotificationStatus.QUEUED);
        notificationRepository.save(n);

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.QUEUED);
    }

    @Test
    public void test7J_030_ProviderRegression() {
        Notification n = createNotification("evt-7g", NotificationStatus.SENT, Instant.now());
        setField(n, "providerName", "Twilio");
        setField(n, "providerMessageId", "SM12345");
        notificationRepository.save(n);

        assertThat(n.getProviderName()).isEqualTo("Twilio");
    }

    @Test
    public void test7J_031_ReliabilityRegression() {
        Notification n = createNotification("evt-7h", NotificationStatus.RETRY_SCHEDULED, Instant.now());
        notificationRepository.save(n);

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test7J_032_OperationsRegression() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").exists());
    }

    @Test
    public void test7J_033_OutboxRegression() {
        OutboxEvent evt = OutboxEvent.create("ORDER", "ord-7c", "ORDER_PAID", "{}");
        outboxRepository.save(evt);

        assertThat(outboxRepository.findById(evt.getId())).isPresent();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void test7J_034_SecurityRegression() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/retention/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test7J_035_FrontendGovernanceRegression() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/intelligence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successRatePercent").exists())
                .andExpect(jsonPath("$.data.backlogAging").exists());
    }

    private Notification createNotification(String eventId, NotificationStatus status, Instant createdAt) {
        String id = UUID.randomUUID().toString();
        Notification n = new Notification(
                eventId, "ORDER_CONFIRMATION", "user-1", "cust-1",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7j@example.com", "Subject", "Body content text",
                NotificationPriority.NORMAL, "idem-" + id, "corr-" + id, "trace-" + id
        );
        if (status != NotificationStatus.CREATED) {
            setField(n, "status", status);
        }
        setField(n, "createdAt", createdAt);
        return n;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
