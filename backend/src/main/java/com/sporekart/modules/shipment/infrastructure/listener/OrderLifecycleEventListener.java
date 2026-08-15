package com.sporekart.modules.shipment.infrastructure.listener;

import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderLifecycleEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderLifecycleEventListener.class);

    private final ShipmentApplicationService shipmentApplicationService;

    public OrderLifecycleEventListener(ShipmentApplicationService shipmentApplicationService) {
        this.shipmentApplicationService = shipmentApplicationService;
    }

    @EventListener
    public void onOrderLifecycleEvent(OrderLifecycleEvent event) {
        if (event.newStatus() == OrderStatus.READY_FOR_FULFILMENT) {
            log.info("Received OrderLifecycleEvent for order {} with status READY_FOR_FULFILMENT, triggering shipment creation", event.orderId());
            try {
                shipmentApplicationService.createShipmentForOrder(event.orderId());
            } catch (Exception e) {
                log.error("Failed to auto-create shipment for order {}: {}", event.orderId(), e.getMessage());
            }
        }
    }
}
