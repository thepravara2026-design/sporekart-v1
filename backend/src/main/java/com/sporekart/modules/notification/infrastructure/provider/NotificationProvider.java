package com.sporekart.modules.notification.infrastructure.provider;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;

public interface NotificationProvider {
    boolean supports(NotificationChannel channel);
    String getProviderName();
    NotificationProviderResult send(Notification notification);
}
