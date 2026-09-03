package com.sporekart.application.outbox.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OutboxEvent publish(String aggregateType, String aggregateId, String eventType, Object payloadObj) {
        java.util.Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        java.util.Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        java.util.Objects.requireNonNull(eventType, "eventType must not be null");

        String payloadJson;
        if (payloadObj instanceof String) {
            payloadJson = (String) payloadObj;
        } else {
            try {
                payloadJson = objectMapper.writeValueAsString(payloadObj);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize outbox event payload for eventType={}: {}", eventType, e.getMessage(), e);
                throw new IllegalArgumentException("Cannot serialize event payload to JSON", e);
            }
        }

        OutboxEvent event = OutboxEvent.create(aggregateType, aggregateId, eventType, payloadJson);
        OutboxEvent saved = outboxEventRepository.save(event);
        log.info("Persisted OutboxEvent id={} aggregateType={} aggregateId={} eventType={} status={}",
                saved.getId(), saved.getAggregateType(), saved.getAggregateId(), saved.getEventType(), saved.getStatus());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> fetchPendingEvents(int limit) {
        return outboxEventRepository.findPendingOrFailedEventsForProcessing(OffsetDateTime.now(), PageRequest.of(0, limit));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<OutboxEvent> markProcessing(String eventId) {
        return outboxEventRepository.findById(eventId).map(event -> {
            if (event.getStatus() == OutboxStatus.PENDING || event.getStatus() == OutboxStatus.FAILED) {
                event.markProcessing();
                return outboxEventRepository.save(event);
            }
            return event;
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(String eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.markProcessed();
            outboxEventRepository.save(event);
            log.info("Successfully marked OutboxEvent id={} as PROCESSED", eventId);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String eventId, String errorMessage) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            int retryCount = event.getRetryCount() + 1;
            // Exponential backoff: 2s, 4s, 8s, 16s...
            int backoffSeconds = (int) Math.pow(2, Math.min(retryCount, 6));
            event.recordFailure(errorMessage, backoffSeconds);
            outboxEventRepository.save(event);
            log.warn("OutboxEvent id={} failure recorded (attempt {}/{}). Status={}, next retry scheduled at {}. Error: {}",
                    eventId, event.getRetryCount(), event.getMaxRetries(), event.getStatus(), event.getScheduledAt(), errorMessage);
        });
    }

    @Transactional(readOnly = true)
    public long countByStatus(OutboxStatus status) {
        return outboxEventRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> findByAggregate(String aggregateType, String aggregateId) {
        return outboxEventRepository.findByAggregateTypeAndAggregateId(aggregateType, aggregateId);
    }
}
