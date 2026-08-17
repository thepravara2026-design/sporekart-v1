package com.sporekart.application.outbox.application;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.dto.OutboxEventDetailResponse;
import com.sporekart.application.outbox.dto.OutboxEventSummaryResponse;
import com.sporekart.application.outbox.dto.OutboxHealthResponse;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OutboxOperationsService {

    private static final Logger log = LoggerFactory.getLogger(OutboxOperationsService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final SecurityAuditService securityAuditService;

    public OutboxOperationsService(OutboxEventRepository outboxEventRepository,
                                   @Autowired(required = false) SecurityAuditService securityAuditService) {
        this.outboxEventRepository = outboxEventRepository;
        this.securityAuditService = securityAuditService;
    }

    @Transactional(readOnly = true)
    public OutboxHealthResponse getOutboxHealth() {
        long pending = outboxEventRepository.countByStatus(OutboxStatus.PENDING);
        long processing = outboxEventRepository.countByStatus(OutboxStatus.PROCESSING);
        long processed = outboxEventRepository.countByStatus(OutboxStatus.PROCESSED);
        long failed = outboxEventRepository.countByStatus(OutboxStatus.FAILED);
        long dead = outboxEventRepository.countByStatus(OutboxStatus.DEAD);

        OffsetDateTime oldestPending = outboxEventRepository.findFirstByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)
                .map(OutboxEvent::getCreatedAt).orElse(null);
        OffsetDateTime oldestFailed = outboxEventRepository.findFirstByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED)
                .map(OutboxEvent::getCreatedAt).orElse(null);
        OffsetDateTime oldestDead = outboxEventRepository.findFirstByStatusOrderByCreatedAtAsc(OutboxStatus.DEAD)
                .map(OutboxEvent::getCreatedAt).orElse(null);

        return OutboxHealthResponse.of(
                pending, processing, processed, failed, dead,
                oldestPending, oldestFailed, oldestDead
        );
    }

    @Transactional(readOnly = true)
    public Page<OutboxEventSummaryResponse> findEvents(OutboxStatus status, Pageable pageable) {
        if (status != null) {
            return outboxEventRepository.findByStatus(status, pageable)
                    .map(OutboxEventSummaryResponse::fromEntity);
        } else {
            return outboxEventRepository.findAll(pageable)
                    .map(OutboxEventSummaryResponse::fromEntity);
        }
    }

    @Transactional(readOnly = true)
    public OutboxEventDetailResponse getEventDetail(String eventId) {
        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + eventId));
        return OutboxEventDetailResponse.fromEntity(event);
    }

    @Transactional
    public OutboxEventDetailResponse replayDeadLetterEvent(String eventId, String requestedBy, String reason) {
        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + eventId));

        if (event.getStatus() != OutboxStatus.DEAD && event.getStatus() != OutboxStatus.FAILED) {
            throw new IllegalStateException("Cannot replay outbox event in status: " + event.getStatus() + ". Only DEAD or FAILED events can be replayed.");
        }

        log.info("OPERATIONAL_REPLAY: Requested by '{}', eventId='{}', previousStatus='{}', reason='{}'",
                requestedBy, eventId, event.getStatus(), reason);

        event.markReplayed(requestedBy, reason);
        OutboxEvent saved = outboxEventRepository.save(event);

        if (securityAuditService != null) {
            try {
                securityAuditService.logEvent(
                        AuditEventType.SECURITY_SYSTEM_ALERT,
                        requestedBy != null ? requestedBy : "ADMIN",
                        eventId,
                        "127.0.0.1",
                        "AdminOutboxConsole",
                        AuditStatus.SUCCESS,
                        "Replayed outbox event id=" + eventId + " (reason: " + reason + ")"
                );
            } catch (Exception e) {
                log.warn("Failed to record security audit log for outbox replay: {}", e.getMessage());
            }
        }

        return OutboxEventDetailResponse.fromEntity(saved);
    }

    @Transactional
    public int recoverStaleProcessingEvents(int timeoutMinutes) {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(Math.max(0, timeoutMinutes));
        List<OutboxEvent> staleEvents = outboxEventRepository.findStaleProcessingEvents(threshold, Pageable.ofSize(100));

        int count = 0;
        for (OutboxEvent event : staleEvents) {
            log.warn("STALE_PROCESSING_RECOVERY: Resetting stuck processing claim for OutboxEvent id='{}', aggregateId='{}', claimedAt='{}'",
                    event.getId(), event.getAggregateId(), event.getScheduledAt());
            event.markStaleReset();
            outboxEventRepository.save(event);
            count++;
        }

        if (count > 0) {
            log.info("Successfully recovered {} stale processing outbox events", count);
        }
        return count;
    }
}
