package com.sporekart.modules.inventory.infrastructure.listener;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryOrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryOrderEventListener.class);

    private final InventoryApplicationService inventoryApplicationService;
    private final ReservationRepository reservationRepository;

    public InventoryOrderEventListener(
            InventoryApplicationService inventoryApplicationService,
            ReservationRepository reservationRepository
    ) {
        this.inventoryApplicationService = inventoryApplicationService;
        this.reservationRepository = reservationRepository;
    }

    @EventListener
    public void onOrderLifecycleEvent(OrderLifecycleEvent event) {
        log.info("Inventory listener received OrderLifecycleEvent for order {} -> status {}", event.orderId(), event.newStatus());

        Optional<StockReservation> reservationOpt = reservationRepository.findByOrderId(event.orderId());
        if (reservationOpt.isEmpty()) {
            log.debug("No stock reservation found for order {}", event.orderId());
            return;
        }

        StockReservation reservation = reservationOpt.get();

        if (event.newStatus() == OrderStatus.CANCELLED && reservation.isActive()) {
            log.info("Releasing stock reservation {} due to order cancellation", reservation.getId());
            try {
                inventoryApplicationService.releaseReservation(reservation.getId(), "ORDER_CANCELLED");
            } catch (Exception e) {
                log.error("Failed to release reservation for cancelled order {}: {}", event.orderId(), e.getMessage());
            }
        } else if ((event.newStatus() == OrderStatus.SHIPPED || event.newStatus() == OrderStatus.DELIVERED) && reservation.isActive()) {
            log.info("Committing stock reservation {} due to order shipment/delivery", reservation.getId());
            try {
                inventoryApplicationService.commitReservation(reservation.getId());
            } catch (Exception e) {
                log.error("Failed to commit reservation for shipped order {}: {}", event.orderId(), e.getMessage());
            }
        }
    }
}
