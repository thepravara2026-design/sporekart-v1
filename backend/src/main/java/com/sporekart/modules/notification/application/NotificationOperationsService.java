package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.NotificationDeliveryWorker;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationDeliveryAttemptRepository;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.security.domain.SecurityAuditEvent;
import com.sporekart.modules.security.infrastructure.persistence.SecurityAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class NotificationOperationsService {

    private static final Logger log = LoggerFactory.getLogger(NotificationOperationsService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final SpringDataJpaNotificationRepository notificationRepository;
    private final SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository;
    private final SecurityAuditEventRepository auditEventRepository;
    private final NotificationOrchestrator orchestrator;
    private final NotificationDeliveryWorker deliveryWorker;
    private final NotificationResilienceService resilienceService;
    private final SecurityAuditService auditService;
    private final NotificationProperties properties;

    public NotificationOperationsService(
            SpringDataJpaNotificationRepository notificationRepository,
            SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository,
            @Autowired(required = false) SecurityAuditEventRepository auditEventRepository,
            NotificationOrchestrator orchestrator,
            @Autowired(required = false) NotificationDeliveryWorker deliveryWorker,
            @Autowired(required = false) NotificationResilienceService resilienceService,
            @Autowired(required = false) SecurityAuditService auditService,
            @Autowired(required = false) NotificationProperties properties) {
        this.notificationRepository = notificationRepository;
        this.attemptRepository = attemptRepository;
        this.auditEventRepository = auditEventRepository;
        this.orchestrator = orchestrator;
        this.deliveryWorker = deliveryWorker;
        this.resilienceService = resilienceService;
        this.auditService = auditService;
        this.properties = properties;
    }

    public Map<String, Object> getOverallHealthSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        BacklogSummaryDto backlog = getBacklogSummary();
        List<AlertConditionDto> activeAlerts = detectAlertConditions(backlog);
        List<Map<String, Object>> providers = getProviderListFromTracker();

        String overallStatus = "HEALTHY";
        boolean anyUnavailable = false;
        boolean anyDegraded = false;
        int openCircuits = 0;

        for (Map<String, Object> p : providers) {
            String status = (String) p.get("status");
            String circuit = (String) p.get("circuitState");
            if ("UNAVAILABLE".equals(status)) anyUnavailable = true;
            if ("DEGRADED".equals(status)) anyDegraded = true;
            if ("OPEN".equals(circuit)) openCircuits++;
        }

        if (anyUnavailable || openCircuits > 1 || backlog.staleProcessingCount() > 50 || backlog.retryBacklogCount() > 1000) {
            overallStatus = "CRITICAL";
        } else if (anyDegraded || openCircuits > 0 || backlog.staleProcessingCount() > 0 || backlog.retryBacklogCount() > 50) {
            overallStatus = "DEGRADED";
        }

        Map<String, Object> resilienceMap = new HashMap<>();
        resilienceMap.put("providers", providers);

        summary.put("overallStatus", overallStatus);
        summary.put("activeAlerts", activeAlerts);
        summary.put("backlog", backlog);
        summary.put("resilience", resilienceMap);
        summary.put("timestamp", Instant.now());
        return summary;
    }

    public BacklogSummaryDto getBacklogSummary() {
        long created = notificationRepository.countByStatus(NotificationStatus.CREATED);
        long queued = notificationRepository.countByStatus(NotificationStatus.QUEUED);
        long processing = notificationRepository.countByStatus(NotificationStatus.PROCESSING);
        long retryScheduled = notificationRepository.countByStatus(NotificationStatus.RETRY_SCHEDULED);
        long sent = notificationRepository.countByStatus(NotificationStatus.SENT);
        long delivered = notificationRepository.countByStatus(NotificationStatus.DELIVERED);
        long failed = notificationRepository.countByStatus(NotificationStatus.FAILED);
        long failedPermanently = notificationRepository.countByStatus(NotificationStatus.FAILED_PERMANENTLY);
        long cancelled = notificationRepository.countByStatus(NotificationStatus.CANCELLED);
        long suppressed = notificationRepository.countByStatus(NotificationStatus.SUPPRESSED);

        Instant cutoffStaleProc = Instant.now().minusSeconds(300);
        long staleProcessing = notificationRepository.findStaleProcessingNotifications(cutoffStaleProc, PageRequest.of(0, 1)).getTotalElements();

        Instant cutoffStaleSent = Instant.now().minusSeconds(600);
        long reconciliationBacklog = notificationRepository.findStaleSentNotifications(cutoffStaleSent, PageRequest.of(0, 1)).getTotalElements();

        Long oldestPendingAgeSeconds = notificationRepository.findOldestPendingCreatedAt()
                .map(t -> Duration.between(t, Instant.now()).getSeconds())
                .orElse(0L);

        Long oldestRetryAgeSeconds = notificationRepository.findOldestRetryCreatedAt()
                .map(t -> Duration.between(t, Instant.now()).getSeconds())
                .orElse(0L);

        Long oldestReconciliationAgeSeconds = notificationRepository.findOldestReconciliationCandidateCreatedAt(cutoffStaleSent)
                .map(t -> Duration.between(t, Instant.now()).getSeconds())
                .orElse(0L);

        return new BacklogSummaryDto(
                created, queued, processing, retryScheduled, sent, delivered,
                failed, failedPermanently, cancelled, suppressed,
                staleProcessing, retryScheduled, reconciliationBacklog,
                oldestPendingAgeSeconds, oldestRetryAgeSeconds, oldestReconciliationAgeSeconds
        );
    }

    public List<AlertConditionDto> detectAlertConditions(BacklogSummaryDto backlog) {
        List<AlertConditionDto> alerts = new ArrayList<>();
        Instant now = Instant.now();
        List<Map<String, Object>> providers = getProviderListFromTracker();

        for (Map<String, Object> p : providers) {
            String name = (String) p.get("name");
            String channel = (String) p.get("channel");
            String circuit = (String) p.get("circuitState");
            String category = (String) p.get("lastFailureCategory");
            String status = (String) p.get("status");

            if ("OPEN".equals(circuit)) {
                alerts.add(new AlertConditionDto(
                        "PROVIDER_CIRCUIT_OPEN", "HIGH", name, channel,
                        "Circuit breaker is OPEN for provider " + name + " (" + channel + ")", now
                ));
            }
            if ("AUTHENTICATION".equals(category) || "CONFIGURATION".equals(category) || "UNAVAILABLE".equals(status)) {
                alerts.add(new AlertConditionDto(
                        "PROVIDER_AUTH_FAILURE", "CRITICAL", name, channel,
                        "Provider " + name + " (" + channel + ") unavailable due to authentication/configuration error", now
                ));
            }
        }

        if (backlog.staleProcessingCount() > 0) {
            alerts.add(new AlertConditionDto(
                    "STALE_PROCESSING", "MEDIUM", "ALL", "ALL",
                    backlog.staleProcessingCount() + " notifications stuck in PROCESSING for >5 minutes", now
            ));
        }

        if (backlog.retryBacklogCount() > 20) {
            alerts.add(new AlertConditionDto(
                    "HIGH_RETRY_BACKLOG", "MEDIUM", "ALL", "ALL",
                    "Retry backlog accumulation: " + backlog.retryBacklogCount() + " notifications scheduled for retry", now
            ));
        }

        if (backlog.reconciliationBacklogCount() > 50) {
            alerts.add(new AlertConditionDto(
                    "RECONCILIATION_BACKLOG", "LOW", "ALL", "ALL",
                    "Reconciliation backlog growing: " + backlog.reconciliationBacklogCount() + " notifications pending delivery confirmation", now
            ));
        }

        return alerts;
    }

    public List<Map<String, Object>> getProviderListFromTracker() {
        List<Map<String, Object>> list = new ArrayList<>();
        if (resilienceService != null) {
            Map<String, Object> summaryMap = resilienceService.getHealthTracker().getHealthSummary(resilienceService.getCircuitBreaker());
            summaryMap.forEach((key, val) -> {
                if (val instanceof Map) {
                    Map<String, Object> pMap = new HashMap<>((Map<String, Object>) val);
                    String[] parts = key.split(":");
                    pMap.put("name", parts[0]);
                    if (parts.length > 1) {
                        pMap.put("channel", parts[1]);
                    }
                    list.add(pMap);
                }
            });
        }
        return list;
    }

    public Page<NotificationDetailDto> getFilteredNotifications(
            NotificationStatus status, NotificationChannel channel, String providerName,
            Instant fromDate, Instant toDate, Pageable pageable) {

        Pageable boundedPageable = boundPageable(pageable);
        Page<Notification> page = notificationRepository.findFilteredNotifications(
                status, channel, providerName, fromDate, toDate, boundedPageable
        );
        return page.map(this::toSanitizedDetailDto);
    }

    public NotificationDetailDto getNotificationDetail(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        return toSanitizedDetailDto(notification);
    }

    public List<NotificationTimelineEventDto> getNotificationTimeline(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        List<NotificationDeliveryAttempt> attempts = attemptRepository.findByNotificationIdOrderByAttemptNumberAsc(notificationId);
        List<NotificationTimelineEventDto> timeline = new ArrayList<>();

        timeline.add(new NotificationTimelineEventDto(
                "CREATED", notification.getCreatedAt(), "SYSTEM", null, null,
                "Notification created for template " + notification.getTemplateCode() + " on channel " + notification.getChannel()
        ));

        for (NotificationDeliveryAttempt att : attempts) {
            timeline.add(new NotificationTimelineEventDto(
                    "ATTEMPT_" + att.getStatus(), att.getAttemptedAt(), att.getProviderName(),
                    att.getProviderMessageId(), att.getErrorMessage(),
                    "Delivery attempt #" + att.getAttemptNumber() + " with provider " + att.getProviderName() + " -> " + att.getStatus()
            ));
        }

        if (notification.getStatus() == NotificationStatus.SENT || notification.getDeliveredAt() != null || notification.getProviderMessageId() != null) {
            Instant sentTime = notification.getLastAttemptAt() != null ? notification.getLastAttemptAt() : notification.getUpdatedAt();
            timeline.add(new NotificationTimelineEventDto(
                    "SENT", sentTime, notification.getProviderName(), notification.getProviderMessageId(), null,
                    "Dispatched to provider " + notification.getProviderName() + " (Message ID: " + notification.getProviderMessageId() + ")"
            ));
        }

        if (notification.getLastReconciliationAt() != null) {
            timeline.add(new NotificationTimelineEventDto(
                    "RECONCILED", notification.getLastReconciliationAt(), notification.getProviderName(),
                    notification.getProviderMessageId(), null,
                    "Provider webhook/status reconciled (Attempt #" + notification.getReconciliationAttemptCount() + ", Provider Status: " + notification.getProviderStatus() + ")"
            ));
        }

        if (notification.getDeliveredAt() != null) {
            timeline.add(new NotificationTimelineEventDto(
                    "DELIVERED", notification.getDeliveredAt(), notification.getProviderName(),
                    notification.getProviderMessageId(), null,
                    "Confirmed delivered to recipient"
            ));
        }

        if (notification.getFailedAt() != null && notification.getStatus() == NotificationStatus.FAILED_PERMANENTLY) {
            timeline.add(new NotificationTimelineEventDto(
                    "FAILED_PERMANENTLY", notification.getFailedAt(), notification.getProviderName(),
                    null, notification.getFailureReason(),
                    "Marked FAILED_PERMANENTLY: " + notification.getFailureReason()
            ));
        }
        if (notification.getCancelledAt() != null) {
            timeline.add(new NotificationTimelineEventDto(
                    "CANCELLED", notification.getCancelledAt(), "ADMIN/USER", null, notification.getFailureReason(),
                    "Cancelled: " + notification.getFailureReason()
            ));
        }
        if (notification.getSuppressedAt() != null) {
            timeline.add(new NotificationTimelineEventDto(
                    "SUPPRESSED", notification.getSuppressedAt(), "POLICY", null, notification.getFailureReason(),
                    "Suppressed: " + notification.getFailureReason()
            ));
        }

        timeline.sort(Comparator.comparing(NotificationTimelineEventDto::timestamp));
        return timeline;
    }

    public Page<NotificationDetailDto> getReconciliationBacklog(Pageable pageable) {
        Pageable bounded = boundPageable(pageable);
        Instant cutoff = Instant.now().minusSeconds(600);
        return notificationRepository.findStaleSentNotifications(cutoff, bounded).map(this::toSanitizedDetailDto);
    }

    @Transactional
    public NotificationDetailDto adminRetry(String adminUserId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.SENT || notification.getStatus() == NotificationStatus.DELIVERED) {
            throw new IllegalStateException("Cannot retry notification in status " + notification.getStatus());
        }

        notification.scheduleRetry("Admin retry requested by " + adminUserId, Instant.now());
        Notification saved = notificationRepository.save(notification);

        orchestrator.processAndDeliver(saved);

        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.SECURITY_SYSTEM_ALERT, adminUserId != null ? adminUserId : "ADMIN", notificationId,
                    "127.0.0.1", "System", AuditStatus.SUCCESS, "Admin manual retry triggered for notification " + notificationId
            );
        }

        return toSanitizedDetailDto(notificationRepository.findById(notificationId).orElse(saved));
    }

    @Transactional
    public NotificationDetailDto adminCancel(String adminUserId, String notificationId, String reason) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.DELIVERED ||
            notification.getStatus() == NotificationStatus.FAILED_PERMANENTLY ||
            notification.getStatus() == NotificationStatus.SUPPRESSED) {
            throw new IllegalStateException("Cannot cancel notification in terminal status " + notification.getStatus());
        }

        notification.markCancelled(reason != null ? reason : "Cancelled by admin request");
        Notification saved = notificationRepository.save(notification);

        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.SECURITY_SYSTEM_ALERT, adminUserId != null ? adminUserId : "ADMIN", notificationId,
                    "127.0.0.1", "System", AuditStatus.SUCCESS, "Admin cancelled notification " + notificationId + ". Reason: " + reason
            );
        }

        return toSanitizedDetailDto(saved);
    }

    @Transactional
    public int triggerStaleRecovery(String adminUserId) {
        int count = 0;
        if (deliveryWorker != null) {
            Instant cutoff = Instant.now().minusSeconds(300);
            List<Notification> stale = notificationRepository.findStaleProcessingNotifications(cutoff, PageRequest.of(0, 50)).getContent();
            count = stale.size();
            deliveryWorker.recoverStaleProcessingNotifications();
        }
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.SECURITY_SYSTEM_ALERT, adminUserId != null ? adminUserId : "ADMIN", "SYSTEM",
                    "127.0.0.1", "System", AuditStatus.SUCCESS, "Stale PROCESSING recovery triggered by admin. Recovered: " + count
            );
        }
        return count;
    }

    public Page<SecurityAuditEvent> getAuditLogs(Pageable pageable) {
        Pageable bounded = boundPageable(pageable);
        if (auditEventRepository != null) {
            return auditEventRepository.findAllByOrderByCreatedAtDesc(bounded);
        }
        return Page.empty();
    }

    private NotificationDetailDto toSanitizedDetailDto(Notification n) {
        String circuitState = "CLOSED";
        if (resilienceService != null && n.getProviderName() != null) {
            circuitState = resilienceService.getCircuitBreaker().getState(n.getProviderName(), n.getChannel()).name();
        }

        return new NotificationDetailDto(
                n.getId(),
                n.getEventId(),
                n.getEventType(),
                n.getUserId(),
                n.getCustomerId(),
                n.getChannel(),
                n.getTemplateCode(),
                n.getTemplateVersion(),
                sanitizeRecipient(n.getRecipient()),
                n.getSubject(),
                n.getStatus(),
                n.getPriority(),
                n.getProviderName(),
                n.getProviderMessageId(),
                n.getProviderEventId(),
                n.getProviderStatus(),
                circuitState,
                n.getAttemptCount(),
                n.getIdempotencyKey(),
                n.getCorrelationId(),
                n.getTraceId(),
                n.getCreatedAt(),
                n.getUpdatedAt(),
                n.getDeliveredAt(),
                n.getFailedAt(),
                n.getFailureReason(),
                n.getLastAttemptAt(),
                n.getNextRetryAt(),
                n.getReconciliationAttemptCount(),
                n.getLastReconciliationAt(),
                n.getCancelledAt(),
                n.getSuppressedAt()
        );
    }

    private String sanitizeRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) return recipient;
        if (recipient.contains("@")) {
            int atIdx = recipient.indexOf("@");
            if (atIdx > 2) {
                return recipient.substring(0, 2) + "***" + recipient.substring(atIdx);
            }
            return recipient;
        }
        if (recipient.length() > 6) {
            return recipient.substring(0, 3) + "***" + recipient.substring(recipient.length() - 3);
        }
        return recipient;
    }

    private Pageable boundPageable(Pageable pageable) {
        if (pageable == null) return PageRequest.of(0, 20);
        int page = pageable.getPageNumber() >= 0 ? pageable.getPageNumber() : 0;
        int size = pageable.getPageSize() > 0 ? Math.min(pageable.getPageSize(), MAX_PAGE_SIZE) : 20;
        return PageRequest.of(page, size, pageable.getSort());
    }

    // DTO records
    public record BacklogSummaryDto(
            long createdCount,
            long queuedCount,
            long processingCount,
            long retryScheduledCount,
            long sentCount,
            long deliveredCount,
            long failedCount,
            long failedPermanentlyCount,
            long cancelledCount,
            long suppressedCount,
            long staleProcessingCount,
            long retryBacklogCount,
            long reconciliationBacklogCount,
            long oldestPendingAgeSeconds,
            long oldestRetryAgeSeconds,
            long oldestReconciliationAgeSeconds
    ) {}

    public record AlertConditionDto(
            String alertType,
            String severity,
            String providerName,
            String channel,
            String message,
            Instant timestamp
    ) {}

    public record NotificationDetailDto(
            String id,
            String eventId,
            String eventType,
            String userId,
            String customerId,
            NotificationChannel channel,
            String templateCode,
            int templateVersion,
            String recipient,
            String subject,
            NotificationStatus status,
            NotificationPriority priority,
            String providerName,
            String providerMessageId,
            String providerEventId,
            String providerStatus,
            String circuitState,
            int attemptCount,
            String idempotencyKey,
            String correlationId,
            String traceId,
            Instant createdAt,
            Instant updatedAt,
            Instant deliveredAt,
            Instant failedAt,
            String failureReason,
            Instant lastAttemptAt,
            Instant nextRetryAt,
            Integer reconciliationAttemptCount,
            Instant lastReconciliationAt,
            Instant cancelledAt,
            Instant suppressedAt
    ) {}

    public record NotificationTimelineEventDto(
            String eventType,
            Instant timestamp,
            String providerName,
            String providerMessageId,
            String details,
            String description
    ) {}
}
