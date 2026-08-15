package com.sporekart.modules.returns.infrastructure.listener;

import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.domain.Return;
import com.sporekart.modules.returns.domain.ReturnRepository;
import com.sporekart.modules.returns.domain.ReturnStatus;
import com.sporekart.modules.shipment.domain.event.ShipmentLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipmentStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentStatusEventListener.class);

    private final ReturnRepository returnRepository;

    public ShipmentStatusEventListener(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @EventListener
    @Async
    public void onShipmentLifecycleEvent(ShipmentLifecycleEvent event) {
        log.info("Received ShipmentLifecycleEvent for shipment {} with new status {}", event.shipmentReference(), event.newStatus());

        List<Return> returns = returnRepository.findByOrderReference(event.orderReference());
        for (Return ret : returns) {
            if (ret.getStatus().isTerminal()) continue;

            String correlationId = event.occurredAt() != null ? event.occurredAt().toString() : event.shipmentReference();
            switch (event.newStatus()) {
                case BOOKED, PICKUP_SCHEDULED -> {
                    if (ret.getStatus() == ReturnStatus.APPROVED) {
                        ret.transitionTo(ReturnStatus.PICKUP_SCHEDULED, "Reverse shipment booked: " + event.shipmentReference(), "SYSTEM", "SYSTEM", correlationId);
                        returnRepository.save(ret);
                    }
                }
                case PICKED_UP -> {
                    if (ret.getStatus() == ReturnStatus.PICKUP_SCHEDULED || ret.getStatus() == ReturnStatus.APPROVED) {
                        ret.transitionTo(ReturnStatus.PICKED_UP, "Carrier picked up return package", "SYSTEM", "SYSTEM", correlationId);
                        returnRepository.save(ret);
                    }
                }
                case IN_TRANSIT, OUT_FOR_DELIVERY -> {
                    if (ret.getStatus() == ReturnStatus.PICKED_UP || ret.getStatus() == ReturnStatus.PICKUP_SCHEDULED) {
                        ret.transitionTo(ReturnStatus.IN_TRANSIT, "Reverse shipment in transit to warehouse", "SYSTEM", "SYSTEM", correlationId);
                        returnRepository.save(ret);
                    }
                }
                case DELIVERED -> {
                    if (ret.getStatus() != ReturnStatus.RECEIVED && ret.getStatus() != ReturnStatus.INSPECTION_PENDING && ret.getStatus() != ReturnStatus.INSPECTED) {
                        ret.markReceived(correlationId);
                        returnRepository.save(ret);
                    }
                }
                default -> {}
            }
        }
    }
}
