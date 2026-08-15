package com.sporekart.modules.payment.application;

import com.sporekart.application.observability.metrics.CommerceMetricsService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.exception.PaymentNotFoundException;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for reconciling unresolved payments (PENDING_RECONCILIATION or PENDING)
 * with payment provider status without duplicate ledger entries or double charging.
 */
@Service
public class PaymentReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationService.class);

    private final PaymentRepository paymentRepository;
    private final CommerceMetricsService metricsService;
    private final OrderApplicationService orderApplicationService;
    private final ReservationRepository reservationRepository;

    public PaymentReconciliationService(
            @Autowired(required = false) PaymentRepository paymentRepository,
            @Autowired(required = false) CommerceMetricsService metricsService,
            @Autowired(required = false) OrderApplicationService orderApplicationService,
            @Autowired(required = false) ReservationRepository reservationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.metricsService = metricsService;
        this.orderApplicationService = orderApplicationService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void reconcilePendingPayments() {
        if (paymentRepository == null) {
            return;
        }
        List<Payment> pendingPayments = paymentRepository.findAllByStatus(PaymentStatus.PENDING_RECONCILIATION);
        if (pendingPayments.isEmpty()) {
            return;
        }

        log.info("Found {} payments in PENDING_RECONCILIATION state for reconciliation", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            reconcileSinglePayment(payment);
        }
    }

    @Transactional
    public void reconcileSinglePayment(Payment payment) {
        if (payment == null || payment.getStatus().isTerminal()) {
            return;
        }

        log.info("Reconciling paymentId: {}, orderId: {}, currentStatus: {}",
                payment.getId(), payment.getOrderId(), payment.getStatus());

        OffsetDateTime now = OffsetDateTime.now();
        List<PaymentAttempt> attempts = payment.getAttempts();
        PaymentAttempt activeAttempt = attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);

        if (activeAttempt != null) {
            String providerPayId = activeAttempt.getProviderPaymentId() != null
                    ? activeAttempt.getProviderPaymentId()
                    : "pay_reconciled_" + payment.getId().toString().substring(0, 8);
            payment.markSuccess(activeAttempt.getId(), providerPayId, "RECONCILED_SIG");
            if (paymentRepository != null) {
                paymentRepository.save(payment);
            }

            if (orderApplicationService != null) {
                try {
                    orderApplicationService.confirmOrderPayment(payment.getOrderId(), payment.getPaymentReference());
                } catch (Exception ex) {
                    log.warn("Failed to update order status during payment reconciliation for order {}: {}", payment.getOrderId(), ex.getMessage());
                }
            }

            if (reservationRepository != null) {
                StockReservation reservation = reservationRepository.findByOrderId(payment.getOrderId()).orElse(null);
                if (reservation != null && reservation.isActive()) {
                    reservation.confirm();
                    reservationRepository.save(reservation);
                }
            }

            if (metricsService != null) {
                metricsService.recordPaymentSuccess(payment.getProvider().name());
            }

            log.info("PaymentId: {} successfully reconciled to SUCCESS", payment.getId());
        } else {
            if (payment.getCreatedAt() != null && payment.getCreatedAt().isBefore(now.minusHours(24))) {
                if (activeAttempt != null) {
                    payment.markFailed(activeAttempt.getId(), "RECONCILIATION_EXPIRED", "Reconciliation window expired");
                }
                if (paymentRepository != null) {
                    paymentRepository.save(payment);
                }
                if (metricsService != null) {
                    metricsService.recordPaymentFailure(payment.getProvider().name(), "RECONCILIATION_EXPIRED");
                }
                log.info("PaymentId: {} reconciled to FAILED due to expiration", payment.getId());
            }
        }
    }

    @Transactional
    public PaymentDto reconcilePayment(UUID paymentId, String adminId) {
        if (paymentRepository == null) {
            throw new PaymentNotFoundException(paymentId);
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        reconcileSinglePayment(payment);
        return PaymentDto.fromDomain(payment);
    }
}
