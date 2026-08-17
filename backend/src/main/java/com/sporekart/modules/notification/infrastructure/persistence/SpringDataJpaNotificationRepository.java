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

    Optional<Notification> findByProviderMessageId(String providerMessageId);

    Optional<Notification> findByProviderEventId(String providerEventId);

    Optional<Notification> findByProviderNameAndProviderEventId(String providerName, String providerEventId);

    List<Notification> findByStatus(NotificationStatus status);

    long countByStatus(NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.CREATED, com.sporekart.modules.notification.domain.NotificationStatus.QUEUED, com.sporekart.modules.notification.domain.NotificationStatus.RETRY_SCHEDULED) AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now) ORDER BY n.createdAt ASC")
    Page<Notification> findEligibleForDelivery(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.status = com.sporekart.modules.notification.domain.NotificationStatus.PROCESSING AND n.updatedAt <= :cutoff ORDER BY n.updatedAt ASC")
    Page<Notification> findStaleProcessingNotifications(@Param("cutoff") Instant cutoff, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.status = com.sporekart.modules.notification.domain.NotificationStatus.SENT AND (n.lastProviderUpdateAt IS NULL OR n.lastProviderUpdateAt <= :cutoff) AND n.createdAt <= :cutoff ORDER BY n.createdAt ASC")
    Page<Notification> findStaleSentNotifications(@Param("cutoff") Instant cutoff, Pageable pageable);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:channel IS NULL OR n.channel = :channel) AND " +
           "(:providerName IS NULL OR UPPER(n.providerName) = UPPER(:providerName)) AND " +
           "(:fromDate IS NULL OR n.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR n.createdAt <= :toDate) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findFilteredNotifications(
            @Param("status") NotificationStatus status,
            @Param("channel") NotificationChannel channel,
            @Param("providerName") String providerName,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT MIN(n.createdAt) FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.CREATED, com.sporekart.modules.notification.domain.NotificationStatus.QUEUED)")
    Optional<Instant> findOldestPendingCreatedAt();

    @Query("SELECT MIN(n.createdAt) FROM Notification n WHERE n.status = com.sporekart.modules.notification.domain.NotificationStatus.RETRY_SCHEDULED")
    Optional<Instant> findOldestRetryCreatedAt();

    @Query("SELECT MIN(n.createdAt) FROM Notification n WHERE n.status = com.sporekart.modules.notification.domain.NotificationStatus.SENT AND (n.lastProviderUpdateAt IS NULL OR n.lastProviderUpdateAt <= :cutoff)")
    Optional<Instant> findOldestReconciliationCandidateCreatedAt(@Param("cutoff") Instant cutoff);

    @Query("SELECT n FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.DELIVERED, com.sporekart.modules.notification.domain.NotificationStatus.FAILED_PERMANENTLY, com.sporekart.modules.notification.domain.NotificationStatus.CANCELLED, com.sporekart.modules.notification.domain.NotificationStatus.SUPPRESSED) AND n.createdAt <= :cutoff ORDER BY n.createdAt ASC")
    Page<Notification> findEligibleForDeletion(@Param("cutoff") Instant cutoff, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.DELIVERED, com.sporekart.modules.notification.domain.NotificationStatus.FAILED_PERMANENTLY, com.sporekart.modules.notification.domain.NotificationStatus.CANCELLED, com.sporekart.modules.notification.domain.NotificationStatus.SUPPRESSED) AND n.createdAt <= :cutoff")
    long countEligibleForDeletion(@Param("cutoff") Instant cutoff);

    @Query("SELECT n FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.DELIVERED, com.sporekart.modules.notification.domain.NotificationStatus.FAILED_PERMANENTLY, com.sporekart.modules.notification.domain.NotificationStatus.CANCELLED, com.sporekart.modules.notification.domain.NotificationStatus.SUPPRESSED) AND n.createdAt <= :cutoff AND (n.body <> '[REDACTED_PAYLOAD]' OR n.body IS NULL) ORDER BY n.createdAt ASC")
    Page<Notification> findEligibleForPayloadMinimization(@Param("cutoff") Instant cutoff, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.DELIVERED, com.sporekart.modules.notification.domain.NotificationStatus.FAILED_PERMANENTLY, com.sporekart.modules.notification.domain.NotificationStatus.CANCELLED, com.sporekart.modules.notification.domain.NotificationStatus.SUPPRESSED) AND n.createdAt <= :cutoff AND (n.body <> '[REDACTED_PAYLOAD]' OR n.body IS NULL)")
    long countEligibleForPayloadMinimization(@Param("cutoff") Instant cutoff);

    @Query("SELECT MIN(n.createdAt) FROM Notification n WHERE n.status IN (com.sporekart.modules.notification.domain.NotificationStatus.DELIVERED, com.sporekart.modules.notification.domain.NotificationStatus.FAILED_PERMANENTLY, com.sporekart.modules.notification.domain.NotificationStatus.CANCELLED, com.sporekart.modules.notification.domain.NotificationStatus.SUPPRESSED)")
    Optional<Instant> findOldestTerminalCreatedAt();

    long countByCreatedAtBetween(Instant fromDate, Instant toDate);

    long countByStatusAndCreatedAtBetween(NotificationStatus status, Instant fromDate, Instant toDate);

    List<Notification> findByCreatedAtBetween(Instant fromDate, Instant toDate);
}
