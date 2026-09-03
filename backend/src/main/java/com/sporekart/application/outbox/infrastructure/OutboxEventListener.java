package com.sporekart.application.outbox.infrastructure;

import com.sporekart.application.outbox.application.OutboxService;
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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventListener {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventListener.class);

    private final OutboxService outboxService;

    public OutboxEventListener(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @EventListener
    public void onOrderLifecycleEvent(OrderLifecycleEvent event) {
        log.debug("OutboxEventListener: intercepting OrderLifecycleEvent for orderId={}", event.orderId());
        outboxService.publish(
                "ORDER",
                event.orderId().toString(),
                "ORDER_" + event.newStatus().name(),
                event
        );
    }

    @EventListener
    public void onShipmentLifecycleEvent(ShipmentLifecycleEvent event) {
        log.debug("OutboxEventListener: intercepting ShipmentLifecycleEvent for shipmentRef={}", event.shipmentReference());
        outboxService.publish(
                "SHIPMENT",
                event.shipmentReference(),
                "SHIPMENT_" + event.newStatus().name(),
                event
        );
    }

    @EventListener
    public void onReturnRequestedEvent(ReturnRequestedEvent event) {
        log.debug("OutboxEventListener: intercepting ReturnRequestedEvent for returnRef={}", event.returnReference());
        outboxService.publish(
                "RETURN",
                event.returnReference(),
                "RETURN_REQUESTED",
                event
        );
    }

    @EventListener
    public void onReturnApprovedEvent(ReturnApprovedEvent event) {
        log.debug("OutboxEventListener: intercepting ReturnApprovedEvent for returnRef={}", event.returnReference());
        outboxService.publish(
                "RETURN",
                event.returnReference(),
                "RETURN_APPROVED",
                event
        );
    }

    @EventListener
    public void onReturnRejectedEvent(ReturnRejectedEvent event) {
        log.debug("OutboxEventListener: intercepting ReturnRejectedEvent for returnRef={}", event.returnReference());
        outboxService.publish(
                "RETURN",
                event.returnReference(),
                "RETURN_REJECTED",
                event
        );
    }

    @EventListener
    public void onRefundProcessedEvent(RefundProcessedEvent event) {
        log.debug("OutboxEventListener: intercepting RefundProcessedEvent for refundRef={}", event.refundReference());
        outboxService.publish(
                "REFUND",
                event.refundReference(),
                "REFUND_PROCESSED",
                event
        );
    }

    @EventListener
    public void onSupportTicketCreatedEvent(SupportTicketCreatedEvent event) {
        log.debug("OutboxEventListener: intercepting SupportTicketCreatedEvent for ticketNumber={}", event.ticketNumber());
        outboxService.publish(
                "SUPPORT",
                event.ticketNumber(),
                "SUPPORT_TICKET_CREATED",
                event
        );
    }

    @EventListener
    public void onReviewSubmittedEvent(ReviewSubmittedEvent event) {
        log.debug("OutboxEventListener: intercepting ReviewSubmittedEvent for reviewId={}", event.reviewId());
        outboxService.publish(
                "REVIEW",
                event.reviewId().toString(),
                "REVIEW_SUBMITTED",
                event
        );
    }
}
