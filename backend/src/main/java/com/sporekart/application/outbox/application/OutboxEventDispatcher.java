package com.sporekart.application.outbox.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.modules.notification.application.NotificationEventProcessor;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.returns.domain.event.RefundProcessedEvent;
import com.sporekart.modules.returns.domain.event.ReturnApprovedEvent;
import com.sporekart.modules.returns.domain.event.ReturnRejectedEvent;
import com.sporekart.modules.returns.domain.event.ReturnRequestedEvent;
import com.sporekart.modules.review.domain.event.ReviewSubmittedEvent;
import com.sporekart.modules.shipment.domain.event.ShipmentLifecycleEvent;
import com.sporekart.modules.support.domain.event.SupportTicketCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OutboxEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventDispatcher.class);

    private final ObjectMapper objectMapper;
    private final NotificationEventProcessor notificationEventProcessor;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public OutboxEventDispatcher(NotificationEventProcessor notificationEventProcessor) {
        this.notificationEventProcessor = notificationEventProcessor;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void dispatch(OutboxEvent event) throws Exception {
        if (processedEventIds.contains(event.getId())) {
            log.info("Idempotency check: OutboxEvent id={} already dispatched in current session", event.getId());
            return;
        }

        String eventType = event.getEventType();
        String payload = event.getPayload();

        log.info("Dispatching OutboxEvent id={} aggregateType={} eventType={}", event.getId(), event.getAggregateType(), eventType);

        if (eventType.startsWith("ORDER_")) {
            OrderLifecycleEvent orderEvent = objectMapper.readValue(payload, OrderLifecycleEvent.class);
            notificationEventProcessor.handleOrderLifecycleEvent(orderEvent);
        } else if (eventType.startsWith("SHIPMENT_")) {
            ShipmentLifecycleEvent shipmentEvent = objectMapper.readValue(payload, ShipmentLifecycleEvent.class);
            notificationEventProcessor.handleShipmentLifecycleEvent(shipmentEvent);
        } else if ("RETURN_REQUESTED".equals(eventType)) {
            ReturnRequestedEvent returnEvent = objectMapper.readValue(payload, ReturnRequestedEvent.class);
            notificationEventProcessor.handleReturnRequestedEvent(returnEvent);
        } else if ("RETURN_APPROVED".equals(eventType)) {
            ReturnApprovedEvent returnEvent = objectMapper.readValue(payload, ReturnApprovedEvent.class);
            notificationEventProcessor.handleReturnApprovedEvent(returnEvent);
        } else if ("RETURN_REJECTED".equals(eventType)) {
            ReturnRejectedEvent returnEvent = objectMapper.readValue(payload, ReturnRejectedEvent.class);
            notificationEventProcessor.handleReturnRejectedEvent(returnEvent);
        } else if ("REFUND_PROCESSED".equals(eventType)) {
            RefundProcessedEvent refundEvent = objectMapper.readValue(payload, RefundProcessedEvent.class);
            notificationEventProcessor.handleRefundProcessedEvent(refundEvent);
        } else if ("SUPPORT_TICKET_CREATED".equals(eventType)) {
            SupportTicketCreatedEvent supportEvent = objectMapper.readValue(payload, SupportTicketCreatedEvent.class);
            notificationEventProcessor.handleSupportTicketCreatedEvent(supportEvent);
        } else if ("REVIEW_SUBMITTED".equals(eventType)) {
            ReviewSubmittedEvent reviewEvent = objectMapper.readValue(payload, ReviewSubmittedEvent.class);
            log.info("OutboxEvent REVIEW_SUBMITTED dispatched for reviewId={}", reviewEvent.reviewId());
        } else {
            log.info("OutboxEvent eventType={} dispatched cleanly", eventType);
        }

        processedEventIds.add(event.getId());
    }

    public boolean isIdempotentlyProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }
}
