package com.sporekart.application.outbox.infrastructure;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status IN ('PENDING', 'FAILED') AND e.scheduledAt <= :now AND e.retryCount < e.maxRetries ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingOrFailedEventsForProcessing(@Param("now") OffsetDateTime now, Pageable pageable);

    List<OutboxEvent> findByAggregateTypeAndAggregateId(String aggregateType, String aggregateId);

    List<OutboxEvent> findByStatus(OutboxStatus status);

    Page<OutboxEvent> findByStatus(OutboxStatus status, Pageable pageable);

    long countByStatus(OutboxStatus status);

    Optional<OutboxEvent> findFirstByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PROCESSING' AND e.scheduledAt <= :threshold ORDER BY e.createdAt ASC")
    List<OutboxEvent> findStaleProcessingEvents(@Param("threshold") OffsetDateTime threshold, Pageable pageable);
}
