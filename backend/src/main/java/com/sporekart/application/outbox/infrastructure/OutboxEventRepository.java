package com.sporekart.application.outbox.infrastructure;

import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status IN ('PENDING', 'FAILED') AND e.scheduledAt <= :now AND e.retryCount < e.maxRetries ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingOrFailedEventsForProcessing(@Param("now") OffsetDateTime now, Pageable pageable);

    List<OutboxEvent> findByAggregateTypeAndAggregateId(String aggregateType, String aggregateId);

    List<OutboxEvent> findByStatus(OutboxStatus status);

    long countByStatus(OutboxStatus status);
}
