package com.sporekart.modules.notification.infrastructure.worker;

import com.sporekart.application.observability.metrics.CommerceMetricsService;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class NotificationReconciliationWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationReconciliationWorker.class);

    private final SpringDataJpaNotificationRepository notificationRepository;
    private final NotificationProperties properties;
    private final CommerceMetricsService metricsService;

    public NotificationReconciliationWorker(SpringDataJpaNotificationRepository notificationRepository,
                                            @Autowired(required = false) NotificationProperties properties,
                                            @Autowired(required = false) CommerceMetricsService metricsService) {
        this.notificationRepository = notificationRepository;
        this.properties = properties;
        this.metricsService = metricsService;
    }

    @Scheduled(fixedDelay = 60000)
    public void reconcileStaleSentNotifications() {
        if (properties != null && !properties.getResilience().isReconciliationEnabled()) {
            return;
        }

        long thresholdSeconds = properties != null ? properties.getResilience().getReconciliationStaleSentSeconds() : 600;
        Instant cutoff = Instant.now().minusSeconds(thresholdSeconds);

        List<Notification> staleSentList = notificationRepository.findStaleSentNotifications(cutoff, PageRequest.of(0, 50)).getContent();
        if (staleSentList.isEmpty()) {
            return;
        }

        log.info("NotificationReconciliationWorker: inspecting {} stale SENT notifications older than {}s", staleSentList.size(), thresholdSeconds);

        for (Notification notification : staleSentList) {
            try {
                reconcileSingleNotification(notification.getId());
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.info("Concurrent update during reconciliation for notification '{}'", notification.getId());
            } catch (Exception ex) {
                log.error("Error reconciling stale SENT notification '{}': {}", notification.getId(), ex.getMessage());
            }
        }
    }

    @Transactional
    public void reconcileSingleNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || notification.getStatus() != NotificationStatus.SENT) {
            return;
        }

        notification.recordReconciliationAttempt();
        Notification saved = notificationRepository.save(notification);

        if (metricsService != null) {
            metricsService.recordNotificationSent(saved.getChannel().name(), saved.getProviderName() != null ? saved.getProviderName() : "UNKNOWN");
        }

        log.info("Reconciliation check completed for SENT notification '{}' (reconciliation attempt #{})", saved.getId(), saved.getReconciliationAttemptCount());
    }
}
