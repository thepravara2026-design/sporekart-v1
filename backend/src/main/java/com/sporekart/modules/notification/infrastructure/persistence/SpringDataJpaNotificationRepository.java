package com.sporekart.modules.notification.infrastructure.persistence;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SpringDataJpaNotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndChannelOrderByCreatedAtDesc(String userId, NotificationChannel channel, Pageable pageable);

    long countByUserIdAndChannelAndReadAtIsNull(String userId, NotificationChannel channel);

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    Optional<Notification> findByEventIdAndChannelAndRecipientAndTemplateVersion(String eventId, NotificationChannel channel, String recipient, int templateVersion);

    List<Notification> findByStatus(NotificationStatus status);

    long countByStatus(NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.CREATED, com.sporekart.modules.notification.domain.NotificationStatus.QUEUED, com.sporekart.modules.notification.domain.NotificationStatus.RETRY_SCHEDULED) AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now) ORDER BY n.createdAt ASC")
    Page<Notification> findEligibleForDelivery(@Param("now") Instant now, Pageable pageable);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
