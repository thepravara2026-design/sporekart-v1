package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationPolicy {

    public record PolicyDecision(
            NotificationCategory category,
            NotificationPriority priority,
            List<NotificationChannel> channels,
            String templateCode
    ) {}

    public PolicyDecision evaluatePolicy(String eventType) {
        if (eventType.startsWith("ORDER_")) {
            return new PolicyDecision(
                    NotificationCategory.ORDER_UPDATES,
                    NotificationPriority.HIGH,
                    List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.SMS),
                    "ORDER_STATUS_UPDATE"
            );
        } else if (eventType.startsWith("SHIPMENT_")) {
            return new PolicyDecision(
                    NotificationCategory.SHIPPING,
                    NotificationPriority.NORMAL,
                    List.of(NotificationChannel.EMAIL, NotificationChannel.WHATSAPP, NotificationChannel.IN_APP),
                    "SHIPMENT_STATUS_UPDATE"
            );
        } else if (eventType.startsWith("RETURN_") || eventType.startsWith("REFUND_")) {
            return new PolicyDecision(
                    NotificationCategory.RETURN_REFUND,
                    NotificationPriority.HIGH,
                    List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.SMS),
                    "RETURN_REQUEST_RECEIVED"
            );
        } else if (eventType.startsWith("SUPPORT_")) {
            return new PolicyDecision(
                    NotificationCategory.SYSTEM_ALERT,
                    NotificationPriority.HIGH,
                    List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                    "SUPPORT_TICKET_CREATED"
            );
        } else {
            return new PolicyDecision(
                    NotificationCategory.PROMOTIONAL,
                    NotificationPriority.NORMAL,
                    List.of(NotificationChannel.IN_APP),
                    "SYSTEM_NOTICE"
            );
        }
    }
}
