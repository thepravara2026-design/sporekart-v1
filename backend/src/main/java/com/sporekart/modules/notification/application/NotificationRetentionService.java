package com.sporekart.modules.notification.application;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.security.infrastructure.persistence.SecurityAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NotificationRetentionService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetentionService.class);

    private final SpringDataJpaNotificationRepository notificationRepository;
    private final OutboxEventRepository outboxRepository;
    private final SecurityAuditEventRepository auditEventRepository;
    private final SecurityAuditService auditService;
    private final NotificationProperties properties;

    // Metrics counters & gauges
    private final AtomicLong lastExecutionTimestamp = new AtomicLong(0);
    private final AtomicLong lastSuccessTimestamp = new AtomicLong(0);
    private final AtomicLong totalRecordsDeleted = new AtomicLong(0);
    private final AtomicLong totalPayloadsMinimized = new AtomicLong(0);
    private final AtomicLong totalOutboxCleaned = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    public NotificationRetentionService(
            SpringDataJpaNotificationRepository notificationRepository,
            OutboxEventRepository outboxRepository,
            @Autowired(required = false) SecurityAuditEventRepository auditEventRepository,
            @Autowired(required = false) SecurityAuditService auditService,
            @Autowired(required = false) NotificationProperties properties) {
        this.notificationRepository = notificationRepository;
        this.outboxRepository = outboxRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditService = auditService;
        this.properties = properties;
    }

    public RetentionHealthDto getRetentionHealth() {
        boolean enabled = properties != null && properties.getRetention().isEnabled();
        boolean dryRun = properties != null && properties.getRetention().isDryRun();
        int notificationDays = properties != null ? properties.getRetention().getNotificationDays() : 30;
        int payloadDays = properties != null ? properties.getRetention().getPayloadDays() : 7;
        int auditDays = properties != null ? properties.getRetention().getAuditDays() : 90;
        int outboxDays = properties != null ? properties.getRetention().getOutboxDays() : 14;

        RetentionPreviewDto preview = getRetentionPreview();

        return new RetentionHealthDto(
                enabled ? "HEALTHY" : "DISABLED",
                enabled,
                dryRun,
                notificationDays,
                payloadDays,
                auditDays,
                outboxDays,
                lastExecutionTimestamp.get() > 0 ? Instant.ofEpochMilli(lastExecutionTimestamp.get()) : null,
                lastSuccessTimestamp.get() > 0 ? Instant.ofEpochMilli(lastSuccessTimestamp.get()) : null,
                preview.eligibleNotificationsForDeletion(),
                preview.eligibleNotificationsForPayloadMinimization(),
                preview.eligibleOutboxEventsForDeletion(),
                totalRecordsDeleted.get(),
                totalPayloadsMinimized.get(),
                totalOutboxCleaned.get(),
                totalFailures.get()
        );
    }

    public RetentionPreviewDto getRetentionPreview() {
        int notificationDays = properties != null ? properties.getRetention().getNotificationDays() : 30;
        int payloadDays = properties != null ? properties.getRetention().getPayloadDays() : 7;
        int outboxDays = properties != null ? properties.getRetention().getOutboxDays() : 14;

        Instant notifCutoff = Instant.now().minus(Duration.ofDays(notificationDays));
        Instant payloadCutoff = Instant.now().minus(Duration.ofDays(payloadDays));
        OffsetDateTime outboxCutoff = OffsetDateTime.now().minus(Duration.ofDays(outboxDays));

        long eligibleDel = notificationRepository.countEligibleForDeletion(notifCutoff);
        long eligibleMin = notificationRepository.countEligibleForPayloadMinimization(payloadCutoff);
        long eligibleOutbox = outboxRepository.countProcessedEventsBefore(outboxCutoff);

        Long oldestTerminalAgeDays = notificationRepository.findOldestTerminalCreatedAt()
                .map(t -> Duration.between(t, Instant.now()).toDays())
                .orElse(0L);

        return new RetentionPreviewDto(
                eligibleDel,
                eligibleMin,
                eligibleOutbox,
                oldestTerminalAgeDays,
                notifCutoff,
                payloadCutoff,
                outboxCutoff.toInstant()
        );
    }

    @Transactional
    public RetentionExecutionResultDto executeRetentionJob(boolean manualOverrideDryRun, String adminUserId) {
        lastExecutionTimestamp.set(System.currentTimeMillis());
        boolean isDryRun = (properties != null && properties.getRetention().isDryRun()) || manualOverrideDryRun;
        int batchSize = properties != null ? properties.getRetention().getBatchSize() : 100;
        int notificationDays = properties != null ? properties.getRetention().getNotificationDays() : 30;
        int payloadDays = properties != null ? properties.getRetention().getPayloadDays() : 7;
        int outboxDays = properties != null ? properties.getRetention().getOutboxDays() : 14;

        Instant notifCutoff = Instant.now().minus(Duration.ofDays(notificationDays));
        Instant payloadCutoff = Instant.now().minus(Duration.ofDays(payloadDays));
        OffsetDateTime outboxCutoff = OffsetDateTime.now().minus(Duration.ofDays(outboxDays));

        if (isDryRun) {
            RetentionPreviewDto preview = getRetentionPreview();
            if (adminUserId != null && auditService != null) {
                auditService.logEvent(
                        AuditEventType.SECURITY_SYSTEM_ALERT, adminUserId, "RETENTION_DRY_RUN",
                        "127.0.0.1", "System", AuditStatus.SUCCESS,
                        "Retention dry-run executed. Eligible deletions: " + preview.eligibleNotificationsForDeletion()
                );
            }
            lastSuccessTimestamp.set(System.currentTimeMillis());
            return new RetentionExecutionResultDto(
                    true, true, 0, 0, 0,
                    preview.eligibleNotificationsForDeletion(),
                    preview.eligibleNotificationsForPayloadMinimization(),
                    preview.eligibleOutboxEventsForDeletion(),
                    Instant.now()
            );
        }

        long minimizedCount = 0;
        long deletedNotifCount = 0;
        long deletedOutboxCount = 0;

        try {
            // 1. Payload Minimization in bounded batch
            Page<Notification> minPage = notificationRepository.findEligibleForPayloadMinimization(payloadCutoff, PageRequest.of(0, batchSize));
            for (Notification n : minPage.getContent()) {
                if (n.getBody() != null && !"[REDACTED_PAYLOAD]".equals(n.getBody())) {
                    String hash = computeSha256(n.getBody());
                    n.minimizePayload(hash);
                    notificationRepository.save(n);
                    minimizedCount++;
                }
            }
            totalPayloadsMinimized.addAndGet(minimizedCount);

            // 2. Terminal Notification Deletion in bounded batch
            Page<Notification> delPage = notificationRepository.findEligibleForDeletion(notifCutoff, PageRequest.of(0, batchSize));
            List<Notification> toDelete = new ArrayList<>();
            for (Notification n : delPage.getContent()) {
                // Double check safety: never delete active notifications
                if (n.getStatus() == NotificationStatus.DELIVERED ||
                    n.getStatus() == NotificationStatus.FAILED_PERMANENTLY ||
                    n.getStatus() == NotificationStatus.CANCELLED ||
                    n.getStatus() == NotificationStatus.SUPPRESSED) {
                    toDelete.add(n);
                }
            }
            if (!toDelete.isEmpty()) {
                notificationRepository.deleteAll(toDelete);
                deletedNotifCount = toDelete.size();
                totalRecordsDeleted.addAndGet(deletedNotifCount);
            }

            // 3. Outbox PROCESSED Events Deletion in bounded batch
            Page<OutboxEvent> outboxPage = outboxRepository.findProcessedEventsBefore(outboxCutoff, PageRequest.of(0, batchSize));
            if (!outboxPage.isEmpty()) {
                outboxRepository.deleteAll(outboxPage.getContent());
                deletedOutboxCount = outboxPage.getContent().size();
                totalOutboxCleaned.addAndGet(deletedOutboxCount);
            }

            lastSuccessTimestamp.set(System.currentTimeMillis());

            if (adminUserId != null && auditService != null) {
                auditService.logEvent(
                        AuditEventType.SECURITY_SYSTEM_ALERT, adminUserId, "RETENTION_CLEANUP",
                        "127.0.0.1", "System", AuditStatus.SUCCESS,
                        "Retention cleanup executed by " + adminUserId + ". Deleted notifications: " + deletedNotifCount +
                                ", Minimized payloads: " + minimizedCount + ", Deleted outbox: " + deletedOutboxCount
                );
            }

            return new RetentionExecutionResultDto(
                    true, false, deletedNotifCount, minimizedCount, deletedOutboxCount, 0, 0, 0, Instant.now()
            );

        } catch (Exception e) {
            totalFailures.incrementAndGet();
            log.error("Retention job execution failed: {}", e.getMessage(), e);
            if (adminUserId != null && auditService != null) {
                auditService.logEvent(
                        AuditEventType.SECURITY_SYSTEM_ALERT, adminUserId, "RETENTION_CLEANUP",
                        "127.0.0.1", "System", AuditStatus.FAILURE,
                        "Retention cleanup failed: " + e.getMessage()
                );
            }
            throw new RuntimeException("Retention cleanup failed: " + e.getMessage(), e);
        }
    }

    public OperationalIntelligenceDto getOperationalIntelligence(String timeWindow, Instant customFromDate, Instant customToDate) {
        Instant now = Instant.now();
        Instant fromDate;
        Instant toDate = customToDate != null ? customToDate : now;

        if ("1h".equalsIgnoreCase(timeWindow)) {
            fromDate = now.minus(Duration.ofHours(1));
        } else if ("7d".equalsIgnoreCase(timeWindow)) {
            fromDate = now.minus(Duration.ofDays(7));
        } else if ("custom".equalsIgnoreCase(timeWindow) && customFromDate != null) {
            fromDate = customFromDate;
        } else { // default 24h
            timeWindow = "24h";
            fromDate = now.minus(Duration.ofHours(24));
        }

        List<Notification> notifs = notificationRepository.findByCreatedAtBetween(fromDate, toDate);
        long total = notifs.size();
        long delivered = 0;
        long failed = 0;
        long failedPermanently = 0;
        long cancelled = 0;
        long suppressed = 0;
        long retried = 0;

        Map<String, ProviderAggregator> providerMap = new HashMap<>();

        for (Notification n : notifs) {
            if (n.getStatus() == NotificationStatus.DELIVERED) delivered++;
            if (n.getStatus() == NotificationStatus.FAILED) failed++;
            if (n.getStatus() == NotificationStatus.FAILED_PERMANENTLY) failedPermanently++;
            if (n.getStatus() == NotificationStatus.CANCELLED) cancelled++;
            if (n.getStatus() == NotificationStatus.SUPPRESSED) suppressed++;
            if (n.getAttemptCount() > 1) retried++;

            String provKey = (n.getProviderName() != null ? n.getProviderName() : "UNKNOWN") + ":" + n.getChannel();
            ProviderAggregator agg = providerMap.computeIfAbsent(provKey, k -> new ProviderAggregator(n.getProviderName() != null ? n.getProviderName() : "UNKNOWN", n.getChannel().name()));
            agg.addNotification(n);
        }

        double successRate = total > 0 ? (delivered * 100.0) / total : 100.0;
        double failureRate = total > 0 ? ((failed + failedPermanently) * 100.0) / total : 0.0;
        double cancellationRate = total > 0 ? (cancelled * 100.0) / total : 0.0;
        double suppressionRate = total > 0 ? (suppressed * 100.0) / total : 0.0;
        double retryRate = total > 0 ? (retried * 100.0) / total : 0.0;

        List<ProviderAggregateDto> providerAggregates = new ArrayList<>();
        providerMap.values().forEach(agg -> providerAggregates.add(agg.toDto()));

        // Backlog aging
        long under1m = 0;
        long between1mAnd5m = 0;
        long between5mAnd15m = 0;
        long over15m = 0;

        List<Notification> activeNotifs = notificationRepository.findByStatus(NotificationStatus.PROCESSING);
        activeNotifs.addAll(notificationRepository.findByStatus(NotificationStatus.RETRY_SCHEDULED));
        activeNotifs.addAll(notificationRepository.findByStatus(NotificationStatus.CREATED));

        for (Notification n : activeNotifs) {
            long ageSeconds = Duration.between(n.getCreatedAt(), now).getSeconds();
            if (ageSeconds < 60) under1m++;
            else if (ageSeconds < 300) between1mAnd5m++;
            else if (ageSeconds < 900) between5mAnd15m++;
            else over15m++;
        }

        BacklogAgingDto backlogAging = new BacklogAgingDto(under1m, between1mAnd5m, between5mAnd15m, over15m);

        return new OperationalIntelligenceDto(
                timeWindow,
                fromDate,
                toDate,
                total,
                delivered,
                failed + failedPermanently,
                cancelled,
                suppressed,
                retried,
                Math.round(successRate * 100.0) / 100.0,
                Math.round(failureRate * 100.0) / 100.0,
                Math.round(cancellationRate * 100.0) / 100.0,
                Math.round(suppressionRate * 100.0) / 100.0,
                Math.round(retryRate * 100.0) / 100.0,
                providerAggregates,
                backlogAging
        );
    }

    private String computeSha256(String input) {
        if (input == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "HASH_ERROR";
        }
    }

    private static class ProviderAggregator {
        private final String providerName;
        private final String channel;
        private long sentCount = 0;
        private long deliveredCount = 0;
        private long failedCount = 0;
        private long totalLatencyMs = 0;
        private long latencySamples = 0;

        public ProviderAggregator(String providerName, String channel) {
            this.providerName = providerName;
            this.channel = channel;
        }

        public void addNotification(Notification n) {
            sentCount++;
            if (n.getStatus() == NotificationStatus.DELIVERED) {
                deliveredCount++;
                if (n.getDeliveredAt() != null && n.getCreatedAt() != null) {
                    long latency = Duration.between(n.getCreatedAt(), n.getDeliveredAt()).toMillis();
                    if (latency >= 0) {
                        totalLatencyMs += latency;
                        latencySamples++;
                    }
                }
            } else if (n.getStatus() == NotificationStatus.FAILED || n.getStatus() == NotificationStatus.FAILED_PERMANENTLY) {
                failedCount++;
            }
        }

        public ProviderAggregateDto toDto() {
            double successRate = sentCount > 0 ? (deliveredCount * 100.0) / sentCount : 100.0;
            double avgLatency = latencySamples > 0 ? (double) totalLatencyMs / latencySamples : 0.0;
            return new ProviderAggregateDto(
                    providerName,
                    channel,
                    sentCount,
                    deliveredCount,
                    failedCount,
                    Math.round(successRate * 100.0) / 100.0,
                    Math.round(avgLatency * 100.0) / 100.0
            );
        }
    }

    // DTO records
    public record RetentionHealthDto(
            String status,
            boolean enabled,
            boolean dryRun,
            int notificationRetentionDays,
            int payloadRetentionDays,
            int auditRetentionDays,
            int outboxRetentionDays,
            Instant lastExecutionTime,
            Instant lastSuccessTime,
            long eligibleNotificationsForDeletion,
            long eligibleNotificationsForPayloadMinimization,
            long eligibleOutboxEventsForDeletion,
            long totalRecordsDeleted,
            long totalPayloadsMinimized,
            long totalOutboxCleaned,
            long totalFailures
    ) {}

    public record RetentionPreviewDto(
            long eligibleNotificationsForDeletion,
            long eligibleNotificationsForPayloadMinimization,
            long eligibleOutboxEventsForDeletion,
            long oldestTerminalRecordAgeDays,
            Instant notificationCutoff,
            Instant payloadCutoff,
            Instant outboxCutoff
    ) {}

    public record RetentionExecutionResultDto(
            boolean success,
            boolean dryRun,
            long deletedNotificationsCount,
            long minimizedPayloadsCount,
            long deletedOutboxEventsCount,
            long eligibleNotificationsCount,
            long eligiblePayloadsCount,
            long eligibleOutboxCount,
            Instant timestamp
    ) {}

    public record ProviderAggregateDto(
            String providerName,
            String channel,
            long sentCount,
            long deliveredCount,
            long failedCount,
            double successRatePercent,
            double averageDeliveryLatencyMs
    ) {}

    public record BacklogAgingDto(
            long under1mCount,
            long between1mAnd5mCount,
            long between5mAnd15mCount,
            long over15mCount
    ) {}

    public record OperationalIntelligenceDto(
            String timeWindow,
            Instant fromDate,
            Instant toDate,
            long totalNotifications,
            long deliveredNotifications,
            long failedNotifications,
            long cancelledNotifications,
            long suppressedNotifications,
            long retriedNotifications,
            double successRatePercent,
            double failureRatePercent,
            double cancellationRatePercent,
            double suppressionRatePercent,
            double retryRatePercent,
            List<ProviderAggregateDto> providerAggregates,
            BacklogAgingDto backlogAging
    ) {}
}
