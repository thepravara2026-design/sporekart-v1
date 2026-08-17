package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationDeliveryAttemptRepository;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

    private final SpringDataJpaNotificationRepository notificationRepository;
    private final SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository;
    private final NotificationTemplateService templateService;
    private final NotificationPreferenceService preferenceService;
    private final NotificationOrchestrator orchestrator;
    private final SecurityAuditService auditService;
    private final NotificationProperties properties;
    private final NotificationResilienceService resilienceService;

    public NotificationApplicationService(SpringDataJpaNotificationRepository notificationRepository,
                                        SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository,
                                        NotificationTemplateService templateService,
                                        NotificationPreferenceService preferenceService,
                                        NotificationOrchestrator orchestrator,
                                        @Autowired(required = false) SecurityAuditService auditService,
                                        @Autowired(required = false) NotificationProperties properties,
                                        @Autowired(required = false) NotificationResilienceService resilienceService) {
        this.notificationRepository = notificationRepository;
        this.attemptRepository = attemptRepository;
        this.templateService = templateService;
        this.preferenceService = preferenceService;
        this.orchestrator = orchestrator;
        this.auditService = auditService;
        this.properties = properties;
        this.resilienceService = resilienceService;
    }

    @Transactional
    public Notification sendNotification(String eventId, String eventType, String userId, String recipient,
                                         NotificationChannel channel, String templateCode,
                                         Map<String, Object> variables) {
        return sendNotification(
                eventId, eventType, userId, userId, channel, templateCode,
                NotificationCategory.ORDER_UPDATES, recipient, variables,
                NotificationPriority.NORMAL, eventId + ":" + channel + ":" + recipient, null, null
        ).orElse(null);
    }

    @Transactional
    public Optional<Notification> sendNotification(String eventId, String eventType, String userId, String customerId,
                                                   NotificationChannel channel, String templateCode,
                                                   NotificationCategory category, String recipient,
                                                   Map<String, Object> variables, NotificationPriority priority,
                                                   String idempotencyKey, String correlationId, String traceId) {

        // 1. Evaluate User Preferences & Mandatory Policy
        if (!preferenceService.isNotificationAllowed(userId, category, channel)) {
            log.info("Notification for category '{}' on channel '{}' suppressed by user preferences for user: {}", category, channel, userId);
            NotificationTemplateService.RenderedTemplate rendered = templateService.renderActiveTemplate(templateCode, channel, "en-US", variables);
            Notification suppressed = new Notification(
                    eventId, eventType, userId, customerId, channel, templateCode,
                    rendered.version(), recipient, rendered.subject(), rendered.body(),
                    priority, idempotencyKey, correlationId, traceId
            );
            suppressed.markSuppressed("Suppressed by user preferences for category " + category);
            Notification savedSuppressed = notificationRepository.save(suppressed);
            return Optional.of(savedSuppressed);
        }

        // 2. Check Idempotency / Duplicate Prevention
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Notification> existing = notificationRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate notification request suppressed via idempotency key: {}", idempotencyKey);
                return existing;
            }
        }

        if (eventId != null && !eventId.isBlank()) {
            Optional<Notification> existingEvent = notificationRepository
                    .findByEventIdAndChannelAndRecipientAndTemplateVersion(eventId, channel, recipient, 1);
            if (existingEvent.isPresent()) {
                log.info("Duplicate notification request suppressed for eventId: {}, channel: {}, recipient: {}", eventId, channel, recipient);
                return existingEvent;
            }
        }

        // 3. Render Template
        NotificationTemplateService.RenderedTemplate rendered = templateService.renderActiveTemplate(templateCode, channel, "en-US", variables);

        // 4. Create Notification Entity
        Notification notification = new Notification(
                eventId, eventType, userId, customerId, channel, templateCode,
                rendered.version(), recipient, rendered.subject(), rendered.body(),
                priority, idempotencyKey, correlationId, traceId
        );

        Notification saved = notificationRepository.save(notification);

        // 5. Audit creation
        if (auditService != null) {
            auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, userId, saved.getId(),
                    "127.0.0.1", "System", AuditStatus.SUCCESS, "Notification created: " + templateCode + " on " + channel);
        }

        // 6. Deliver via Orchestrator
        Notification finalNotification = orchestrator.processAndDeliver(saved);
        return Optional.of(finalNotification);
    }

    @Transactional
    public Notification cancelNotification(String userId, String notificationId, String reason) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (userId != null && !userId.equalsIgnoreCase(notification.getUserId())) {
            throw new SecurityException("Unauthorized access to notification: " + notificationId);
        }

        if (notification.getStatus() == NotificationStatus.SENT || notification.getStatus() == NotificationStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel notification in status " + notification.getStatus());
        }

        notification.markCancelled(reason != null ? reason : "Cancelled by user/admin request");
        Notification saved = notificationRepository.save(notification);

        if (auditService != null) {
            auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, userId != null ? userId : "ADMIN", saved.getId(),
                    "127.0.0.1", "System", AuditStatus.SUCCESS, "Notification cancelled: " + notificationId);
        }

        return saved;
    }

    @Transactional
    public Notification retryNotificationAdmin(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.SENT || notification.getStatus() == NotificationStatus.DELIVERED) {
            throw new IllegalStateException("Cannot retry notification in status " + notification.getStatus());
        }

        notification.transitionTo(NotificationStatus.PROCESSING);
        Notification saved = notificationRepository.save(notification);
        return orchestrator.processAndDeliver(saved);
    }

    public Map<String, Object> getNotificationHealthMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("createdCount", notificationRepository.countByStatus(NotificationStatus.CREATED));
        metrics.put("queuedCount", notificationRepository.countByStatus(NotificationStatus.QUEUED));
        metrics.put("processingCount", notificationRepository.countByStatus(NotificationStatus.PROCESSING));
        metrics.put("sentCount", notificationRepository.countByStatus(NotificationStatus.SENT));
        metrics.put("deliveredCount", notificationRepository.countByStatus(NotificationStatus.DELIVERED));
        metrics.put("failedCount", notificationRepository.countByStatus(NotificationStatus.FAILED));
        metrics.put("retryScheduledCount", notificationRepository.countByStatus(NotificationStatus.RETRY_SCHEDULED));
        metrics.put("failedPermanentlyCount", notificationRepository.countByStatus(NotificationStatus.FAILED_PERMANENTLY));
        metrics.put("cancelledCount", notificationRepository.countByStatus(NotificationStatus.CANCELLED));
        metrics.put("suppressedCount", notificationRepository.countByStatus(NotificationStatus.SUPPRESSED));
        metrics.put("totalCount", notificationRepository.count());

        if (properties != null) {
            metrics.put("providers", Map.of(
                    "email", Map.of("mode", properties.getEmail().getMode(), "provider", properties.getEmail().getProvider(), "configured", properties.getEmail().getApiKey() != null),
                    "sms", Map.of("mode", properties.getSms().getMode(), "provider", properties.getSms().getProvider(), "configured", properties.getSms().getAccountSid() != null),
                    "whatsapp", Map.of("mode", properties.getWhatsapp().getMode(), "provider", properties.getWhatsapp().getProvider(), "configured", properties.getWhatsapp().getPhoneNumberId() != null),
                    "push", Map.of("mode", properties.getPush().getMode(), "provider", properties.getPush().getProvider(), "configured", true)
            ));
        }

        if (resilienceService != null) {
            metrics.put("resilience", resilienceService.getHealthTracker().getHealthSummary(resilienceService.getCircuitBreaker()));
        }

        return metrics;
    }

    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<Notification> getUserInAppNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndChannelOrderByCreatedAtDesc(userId, NotificationChannel.IN_APP, pageable);
    }

    public long getUnreadInAppCount(String userId) {
        return notificationRepository.countByUserIdAndChannelAndReadAtIsNull(userId, NotificationChannel.IN_APP);
    }

    @Transactional
    public Notification markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (userId != null && !userId.equalsIgnoreCase(notification.getUserId())) {
            throw new SecurityException("Unauthorized access to notification: " + notificationId);
        }

        notification.markRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndChannelOrderByCreatedAtDesc(userId, NotificationChannel.IN_APP, Pageable.unpaged())
                .getContent();
        for (Notification n : unread) {
            n.markRead();
        }
        notificationRepository.saveAll(unread);
    }

    public Page<Notification> getAllNotificationsAdmin(Pageable pageable) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<NotificationDeliveryAttempt> getDeliveryAttempts(String notificationId) {
        return attemptRepository.findByNotificationIdOrderByAttemptNumberAsc(notificationId);
    }
}
