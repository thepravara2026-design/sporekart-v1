package com.sporekart.modules.notification.infrastructure.provider;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InAppNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationProvider.class);

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.IN_APP;
    }

    @Override
    public String getProviderName() {
        return "InAppNotificationProvider";
    }

    @Override
    public NotificationProviderResult send(Notification notification) {
        log.info("Persisting IN_APP notification for user '{}'", notification.getUserId());
        return NotificationProviderResult.success("INAPP-MSG-" + UUID.randomUUID().toString().substring(0, 8));
    }
}
