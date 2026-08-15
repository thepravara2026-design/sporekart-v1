package com.sporekart.modules.order.infrastructure.scheduler;

import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class OrderExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpiryScheduler.class);

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final OrderApplicationService orderApplicationService;

    public OrderExpiryScheduler(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            OrderApplicationService orderApplicationService
    ) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.orderApplicationService = orderApplicationService;
    }

    @Scheduled(cron = "${sporekart.order.expiry-cron:0 * * * * *}")
    public void cleanupExpiredOrders() {
        log.trace("Executing periodic order expiration cleanup check...");
        List<Order> pendingOrders = orderRepository.findByStatusIn(List.of(OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING));
        OffsetDateTime now = OffsetDateTime.now();

        int expiredCount = 0;
        for (Order order : pendingOrders) {
            StockReservation reservation = reservationRepository.findByOrderId(order.getId()).orElse(null);
            if (reservation != null && (reservation.isExpired(now) || !reservation.isActive())) {
                try {
                    orderApplicationService.expireOrder(order.getId(), "Associated inventory reservation expired");
                    expiredCount++;
                } catch (Exception e) {
                    log.error("Error expiring order {}: {}", order.getId(), e.getMessage());
                }
            }
        }

        if (expiredCount > 0) {
            log.info("Expired {} pending orders due to reservation expiry", expiredCount);
        }
    }
}
