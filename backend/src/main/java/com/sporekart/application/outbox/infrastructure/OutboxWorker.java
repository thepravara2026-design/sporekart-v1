package com.sporekart.application.outbox.infrastructure;

import com.sporekart.application.outbox.application.OutboxEventDispatcher;
import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxService outboxService;
    private final OutboxEventDispatcher outboxEventDispatcher;
    private boolean enabled = true;

    public OutboxWorker(OutboxService outboxService, OutboxEventDispatcher outboxEventDispatcher) {
        this.outboxService = outboxService;
        this.outboxEventDispatcher = outboxEventDispatcher;
    }

    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        if (!enabled) {
            return;
        }

        try {
            List<OutboxEvent> pendingEvents = outboxService.fetchPendingEvents(20);
            if (pendingEvents.isEmpty()) {
                return;
            }

            log.info("OutboxWorker: processing {} pending outbox events", pendingEvents.size());

            for (OutboxEvent pending : pendingEvents) {
                processSingleEvent(pending);
            }
        } catch (Exception e) {
            log.error("OutboxWorker exception during scheduled execution cycle: {}", e.getMessage(), e);
        }
    }

    public void processSingleEvent(OutboxEvent pending) {
        Optional<OutboxEvent> processingOpt = outboxService.markProcessing(pending.getId());
        if (processingOpt.isEmpty()) {
            return;
        }

        OutboxEvent event = processingOpt.get();
        try {
            outboxEventDispatcher.dispatch(event);
            outboxService.markProcessed(event.getId());
        } catch (Exception e) {
            log.error("Failed to process OutboxEvent id={}: {}", event.getId(), e.getMessage(), e);
            outboxService.recordFailure(event.getId(), e.getMessage());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
