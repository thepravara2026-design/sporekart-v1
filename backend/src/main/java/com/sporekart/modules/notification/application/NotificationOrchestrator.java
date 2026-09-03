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

import java.time.Instant;
import java.util.List;

@Service
public class NotificationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(NotificationOrchestrator.class);

    private final List<NotificationProvider> providers;
    private final SpringDataJpaNotificationRepository notificationRepository;
    private final SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository;
    private final ResilientExecutor resilientExecutor;
    private final CommerceMetricsService metricsService;
    private final NotificationResilienceService resilienceService;

    public NotificationOrchestrator(List<NotificationProvider> providers,
                                    SpringDataJpaNotificationRepository notificationRepository,
                                    SpringDataJpaNotificationDeliveryAttemptRepository attemptRepository,
                                    ResilientExecutor resilientExecutor,
                                    @Autowired(required = false) CommerceMetricsService metricsService,
                                    @Autowired(required = false) NotificationResilienceService resilienceService) {
        this.providers = providers;
        this.notificationRepository = notificationRepository;
        this.attemptRepository = attemptRepository;
        this.resilientExecutor = resilientExecutor;
        this.metricsService = metricsService;
        this.resilienceService = resilienceService;
    }

    @Transactional
    public Notification processAndDeliver(Notification notification) {
        NotificationProvider provider = providers.stream()
                .filter(p -> p.supports(notification.getChannel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No notification provider found for channel: " + notification.getChannel()));

        String providerName = provider.getProviderName();

        // 1. Circuit Breaker Check
        if (resilienceService != null && !resilienceService.allowExecution(providerName, notification.getChannel())) {
            log.warn("Circuit Breaker OPEN for provider '{}' on channel '{}', scheduling retry for notification '{}'",
                    providerName, notification.getChannel(), notification.getId());
            
            Instant nextRetry = resilienceService.calculateNextRetrySchedule(notification.getAttemptCount() + 1, "Circuit breaker OPEN");
            notification.scheduleRetry("Circuit breaker OPEN for provider " + providerName, nextRetry);
            Notification saved = notificationRepository.save(notification);

            NotificationDeliveryAttempt attempt = new NotificationDeliveryAttempt(
                    saved.getId(),
                    saved.getAttemptCount(),
                    providerName,
                    saved.getStatus(),
                    null,
                    "Circuit breaker OPEN",
                    0L
            );
            attemptRepository.save(attempt);

            if (metricsService != null) {
                metricsService.recordNotificationFailed(saved.getChannel().name(), providerName, "CIRCUIT_OPEN");
            }

            return saved;
        }

        // 2. Mark Processing
        notification.markProcessing(providerName);
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
            boolean isTransient = resilientExecutor == null || resilientExecutor.isRetryable(ex);
            result = NotificationProviderResult.failure(ex.getMessage(), isTransient);
        }

        long durationMs = System.currentTimeMillis() - startTime;

        if (result.success()) {
            if (resilienceService != null) {
                resilienceService.recordSuccess(providerName, savedNotification.getChannel(), durationMs);
            }
            savedNotification.markSent(result.providerMessageId());
            if (savedNotification.getChannel() == NotificationChannel.IN_APP) {
                savedNotification.markDelivered(result.providerMessageId());
            }
            if (metricsService != null) {
                metricsService.recordNotificationSent(savedNotification.getChannel().name(), providerName);
                if (savedNotification.getStatus() == NotificationStatus.DELIVERED) {
                    metricsService.recordNotificationDelivered(savedNotification.getChannel().name(), providerName);
                }
            }
        } else {
            ProviderFailureCategory category = resilienceService != null
                    ? resilienceService.classifyFailure(result.errorMessage(), result.transientFailure())
                    : (result.transientFailure() ? ProviderFailureCategory.TRANSIENT : ProviderFailureCategory.PERMANENT);

            boolean isAuthOrConfig = category == ProviderFailureCategory.AUTHENTICATION || category == ProviderFailureCategory.CONFIGURATION;
            boolean isPermanent = !result.transientFailure() || isAuthOrConfig;

            if (resilienceService != null) {
                resilienceService.recordFailure(providerName, savedNotification.getChannel(), result.errorMessage(), !isPermanent, durationMs);
            }

            if (isPermanent) {
                savedNotification.markFailed(result.errorMessage(), true);
            } else {
                Instant nextRetry = resilienceService != null
                        ? resilienceService.calculateNextRetrySchedule(savedNotification.getAttemptCount(), result.errorMessage())
                        : Instant.now().plusSeconds((long) Math.pow(2, savedNotification.getAttemptCount()));
                savedNotification.scheduleRetry(result.errorMessage(), nextRetry);
            }

            if (metricsService != null) {
                metricsService.recordNotificationFailed(savedNotification.getChannel().name(), providerName, category.name());
            }
        }

        Notification finalSaved = notificationRepository.save(savedNotification);

        NotificationDeliveryAttempt attempt = new NotificationDeliveryAttempt(
                finalSaved.getId(),
                finalSaved.getAttemptCount(),
                providerName,
                finalSaved.getStatus(),
                result.providerMessageId(),
                result.errorMessage(),
                durationMs
        );
        attemptRepository.save(attempt);

        return finalSaved;
    }
}
