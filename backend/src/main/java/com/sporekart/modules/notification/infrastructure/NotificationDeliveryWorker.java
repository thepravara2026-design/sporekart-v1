package com.sporekart.modules.notification.infrastructure;

import com.sporekart.modules.notification.application.NotificationOrchestrator;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class NotificationDeliveryWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryWorker.class);

    private final SpringDataJpaNotificationRepository notificationRepository;
    private final NotificationOrchestrator orchestrator;

    public NotificationDeliveryWorker(SpringDataJpaNotificationRepository notificationRepository,
                                      NotificationOrchestrator orchestrator) {
        this.notificationRepository = notificationRepository;
        this.orchestrator = orchestrator;
    }

    @Scheduled(fixedDelay = 2000)
    public void processPendingNotifications() {
        List<Notification> eligibleList = notificationRepository.findEligibleForDelivery(Instant.now(), PageRequest.of(0, 50)).getContent();
        if (eligibleList.isEmpty()) {
            return;
        }

        log.info("NotificationDeliveryWorker: processing {} eligible notifications", eligibleList.size());

        for (Notification notification : eligibleList) {
            try {
                processSingleNotification(notification.getId());
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.info("Concurrent claim detected for notification '{}', skipping worker iteration", notification.getId());
            } catch (Exception ex) {
                log.error("Error delivering notification '{}': {}", notification.getId(), ex.getMessage());
            }
        }
    }

    @Transactional
    public void processSingleNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return;
        }

        if (notification.getStatus() == NotificationStatus.SENT ||
            notification.getStatus() == NotificationStatus.DELIVERED ||
            notification.getStatus() == NotificationStatus.FAILED_PERMANENTLY ||
            notification.getStatus() == NotificationStatus.CANCELLED ||
            notification.getStatus() == NotificationStatus.SUPPRESSED) {
            return;
        }

        // Bounded max retries check (Max 3 attempts)
        if (notification.getAttemptCount() >= 3) {
            log.warn("Notification '{}' reached max retry attempts ({}), marking FAILED_PERMANENTLY", notification.getId(), notification.getAttemptCount());
            notification.markFailed("Exceeded maximum delivery retries (3)", true);
            notificationRepository.save(notification);
            return;
        }

        orchestrator.processAndDeliver(notification);
    }
}
