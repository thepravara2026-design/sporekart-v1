package com.sporekart.modules.notification.application;

import com.sporekart.application.observability.metrics.CommerceMetricsService;
import com.sporekart.application.resilience.ResilientExecutor;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationDeliveryAttemptRepository;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.notification.infrastructure.provider.NotificationProvider;
import com.sporekart.modules.notification.infrastructure.provider.NotificationProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(NotificationOrchestrator.class);

    private final List<NotificationProvider> providers;
    private final SpringDataJpaNotificationRepository notificationRepository;
    private final SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository;
    private final ResilientExecutor resilientExecutor;
    private final CommerceMetricsService metricsService;

    public NotificationOrchestrator(List<NotificationProvider> providers,
                                    SpringDataJpaNotificationRepository notificationRepository,
                                    SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository,
                                    ResilientExecutor resilientExecutor,
                                    @Autowired(required = false) CommerceMetricsService metricsService) {
        this.providers = providers;
        this.notificationRepository = notificationRepository;
        this.attemptRepository = attemptRepository;
        this.resilientExecutor = resilientExecutor;
        this.metricsService = metricsService;
    }

    @Transactional
    public Notification processAndDeliver(Notification notification) {
        NotificationProvider provider = providers.stream()
                .filter(p -> p.supports(notification.getChannel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No notification provider found for channel: " + notification.getChannel()));

        notification.markProcessing(provider.getProviderName());
        Notification savedNotification = notificationRepository.save(notification);

        long startTime = System.currentTimeMillis();
        NotificationProviderResult result;

        try {
            if (resilientExecutor != null) {
                result = resilientExecutor.executeWithRetry("NOTIFICATION_SEND_" + notification.getChannel(), null, () -> provider.send(savedNotification));
            } else {
                result = provider.send(savedNotification);
            }
        } catch (Exception ex) {
            log.error("Provider execution failed for notification '{}': {}", savedNotification.getId(), ex.getMessage());
            boolean isTransient = resilientExecutor != null && resilientExecutor.isRetryable(ex);
            result = NotificationProviderResult.failure(ex.getMessage(), isTransient);
        }

        long durationMs = System.currentTimeMillis() - startTime;

        if (result.success()) {
            savedNotification.markSent(result.providerMessageId());
            if (savedNotification.getChannel() == NotificationChannel.IN_APP) {
                savedNotification.markDelivered(result.providerMessageId());
            }
            if (metricsService != null) {
                metricsService.recordNotificationSent(savedNotification.getChannel().name(), provider.getProviderName());
                if (savedNotification.getStatus() == NotificationStatus.DELIVERED) {
                    metricsService.recordNotificationDelivered(savedNotification.getChannel().name(), provider.getProviderName());
                }
            }
        } else {
            savedNotification.markFailed(result.errorMessage(), !result.transientFailure());
            if (metricsService != null) {
                metricsService.recordNotificationFailed(savedNotification.getChannel().name(), provider.getProviderName(), result.errorMessage());
            }
        }

        Notification finalSaved = notificationRepository.save(savedNotification);

        NotificationDeliveryAttempt attempt = new NotificationDeliveryAttempt(
                finalSaved.getId(),
                finalSaved.getAttemptCount(),
                provider.getProviderName(),
                finalSaved.getStatus(),
                result.providerMessageId(),
                result.errorMessage(),
                durationMs
        );
        attemptRepository.save(attempt);

        return finalSaved;
    }
}
