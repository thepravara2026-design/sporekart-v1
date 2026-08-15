package com.sporekart.modules.notification.infrastructure.persistence;

import com.sporekart.modules.notification.domain.NotificationDeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJpaNotificationDeliveryAttemptRepository extends JpaRepository<NotificationDeliveryAttempt, String> {
    List<NotificationDeliveryAttempt> findByNotificationIdOrderByAttemptNumberAsc(String notificationId);
}
