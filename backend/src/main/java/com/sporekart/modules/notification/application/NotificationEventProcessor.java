package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventProcessor.class);

    private final NotificationApplicationService notificationApplicationService;

    public NotificationEventProcessor(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @EventListener
    @Async
    public void handleOrderLifecycleEvent(OrderLifecycleEvent event) {
        log.info("Processing notification for OrderLifecycleEvent: orderNumber={}, newStatus={}", event.orderNumber(), event.newStatus());
        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", event.orderId().toString());
        vars.put("orderNumber", event.orderNumber());
        vars.put("status", event.newStatus().name());
        vars.put("reason", event.reason() != null ? event.reason() : "");

        String eventId = UUID.randomUUID().toString();
        String eventType = "ORDER_" + event.newStatus().name();
        String recipient = event.actorId() != null ? event.actorId() : "customer@sporekart.com";

        // Dispatch EMAIL notification
        notificationApplicationService.sendNotification(
                eventId, eventType, event.actorId(), event.actorId(),
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                recipient, vars, NotificationPriority.NORMAL,
                eventId + "-EMAIL", null, null
        );

        // Dispatch IN_APP notification
        notificationApplicationService.sendNotification(
                eventId, eventType, event.actorId(), event.actorId(),
                NotificationChannel.IN_APP, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                recipient, vars, NotificationPriority.NORMAL,
                eventId + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleShipmentLifecycleEvent(ShipmentLifecycleEvent event) {
        log.info("Processing notification for ShipmentLifecycleEvent: shipmentRef={}, newStatus={}", event.shipmentReference(), event.newStatus());
        Map<String, Object> vars = new HashMap<>();
        vars.put("shipmentReference", event.shipmentReference());
        vars.put("trackingNumber", event.awb() != null ? event.awb() : "");
        vars.put("carrier", event.courierName() != null ? event.courierName() : "");
        vars.put("status", event.newStatus().name());

        String eventId = UUID.randomUUID().toString();
        String eventType = "SHIPMENT_" + event.newStatus().name();
        String recipient = event.customerId() != null ? event.customerId() : "customer@sporekart.com";

        notificationApplicationService.sendNotification(
                eventId, eventType, event.customerId(), event.customerId(),
                NotificationChannel.EMAIL, "SHIPMENT_UPDATE", NotificationCategory.SHIPPING,
                recipient, vars, NotificationPriority.NORMAL,
                eventId + "-EMAIL", null, null
        );

        notificationApplicationService.sendNotification(
                eventId, eventType, event.customerId(), event.customerId(),
                NotificationChannel.IN_APP, "SHIPMENT_UPDATE", NotificationCategory.SHIPPING,
                recipient, vars, NotificationPriority.NORMAL,
                eventId + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleReturnRequestedEvent(ReturnRequestedEvent event) {
        log.info("Processing notification for ReturnRequestedEvent: returnRef={}", event.returnReference());
        Map<String, Object> vars = Map.of(
                "returnReference", event.returnReference(),
                "orderReference", event.orderReference()
        );

        String eventId = UUID.randomUUID().toString();
        notificationApplicationService.sendNotification(
                eventId, "RETURN_REQUESTED", event.customerId(), event.customerId(),
                NotificationChannel.EMAIL, "RETURN_REQUESTED", NotificationCategory.RETURN_REFUND,
                event.customerId(), vars, NotificationPriority.NORMAL,
                eventId + "-EMAIL", null, null
        );
    }

    @EventListener
    @Async
    public void handleReturnApprovedEvent(ReturnApprovedEvent event) {
        log.info("Processing notification for ReturnApprovedEvent: returnRef={}", event.returnReference());
        Map<String, Object> vars = Map.of(
                "returnReference", event.returnReference(),
                "orderReference", event.orderReference()
        );

        String eventId = UUID.randomUUID().toString();
        notificationApplicationService.sendNotification(
                eventId, "RETURN_APPROVED", event.customerId(), event.customerId(),
                NotificationChannel.EMAIL, "RETURN_APPROVED", NotificationCategory.RETURN_REFUND,
                event.customerId(), vars, NotificationPriority.NORMAL,
                eventId + "-EMAIL", null, null
        );
    }

    @EventListener
    @Async
    public void handleReturnRejectedEvent(ReturnRejectedEvent event) {
        log.info("Processing notification for ReturnRejectedEvent: returnRef={}", event.returnReference());
        Map<String, Object> vars = Map.of(
                "returnReference", event.returnReference(),
                "reason", event.reason() != null ? event.reason() : ""
        );

        String eventId = UUID.randomUUID().toString();
        notificationApplicationService.sendNotification(
                eventId, "RETURN_REJECTED", event.customerId(), event.customerId(),
                NotificationChannel.EMAIL, "RETURN_REJECTED", NotificationCategory.RETURN_REFUND,
                event.customerId(), vars, NotificationPriority.NORMAL,
                eventId + "-EMAIL", null, null
        );
    }

    @EventListener
    @Async
    public void handleRefundProcessedEvent(RefundProcessedEvent event) {
        log.info("Processing notification for RefundProcessedEvent: refundRef={}", event.refundReference());
        Map<String, Object> vars = Map.of(
                "refundReference", event.refundReference(),
                "amount", event.amount().toString()
        );

        String eventId = UUID.randomUUID().toString();
        notificationApplicationService.sendNotification(
                eventId, "REFUND_PROCESSED", event.customerId(), event.customerId(),
                NotificationChannel.EMAIL, "REFUND_PROCESSED", NotificationCategory.PAYMENT,
                event.customerId(), vars, NotificationPriority.HIGH,
                eventId + "-EMAIL", null, null
        );
    }

    @EventListener
    @Async
    public void handleSupportTicketCreatedEvent(SupportTicketCreatedEvent event) {
        log.info("Processing notification for SupportTicketCreatedEvent: ticketNumber={}", event.ticketNumber());
        Map<String, Object> vars = Map.of("ticketNumber", event.ticketNumber(), "category", event.category().name());

        String eventId = UUID.randomUUID().toString();
        notificationApplicationService.sendNotification(
                eventId, "SUPPORT_TICKET_CREATED", event.customerId(), event.customerId(),
                NotificationChannel.IN_APP, "SUPPORT_TICKET_CREATED", NotificationCategory.ORDER_UPDATES,
                event.customerId(), vars, NotificationPriority.NORMAL,
                eventId + "-INAPP", null, null
        );
    }
}
