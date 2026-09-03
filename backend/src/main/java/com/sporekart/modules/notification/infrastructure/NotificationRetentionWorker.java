package com.sporekart.modules.notification.infrastructure;

import com.sporekart.modules.notification.application.NotificationRetentionService;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetentionWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetentionWorker.class);

    private final NotificationRetentionService retentionService;
    private final NotificationProperties properties;

    public NotificationRetentionWorker(
            NotificationRetentionService retentionService,
            @Autowired(required = false) NotificationProperties properties) {
        this.retentionService = retentionService;
        this.properties = properties;
    }

    @Scheduled(cron = "${app.notification.retention.cron:0 0 2 * * ?}")
    public void runScheduledRetention() {
        if (properties != null && !properties.getRetention().isEnabled()) {
            log.debug("Notification retention worker is disabled by configuration");
            return;
        }

        log.info("Starting scheduled notification retention cleanup worker...");
        try {
            var result = retentionService.executeRetentionJob(false, "SYSTEM_RETENTION_WORKER");
            log.info("Notification retention worker finished successfully. Deleted: {}, Minimized: {}, Outbox Deleted: {}",
                    result.deletedNotificationsCount(), result.minimizedPayloadsCount(), result.deletedOutboxEventsCount());
        } catch (Exception e) {
            log.error("Error during notification retention worker execution: {}", e.getMessage(), e);
        }
    }
}
