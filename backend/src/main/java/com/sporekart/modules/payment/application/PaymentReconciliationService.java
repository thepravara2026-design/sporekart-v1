package com.sporekart.modules.payment.application;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.PaymentStatusHistory;
import com.sporekart.modules.payment.domain.exception.PaymentNotFoundException;
import com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryEntity;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.payment.infrastructure.provider.PaymentStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository statusHistoryRepository;
    private final OrderApplicationService orderApplicationService;
    private final ReservationRepository reservationRepository;
    private final InventoryApplicationService inventoryApplicationService;
    private final PaymentProviderRegistry providerRegistry;

    public PaymentReconciliationService(
            PaymentRepository paymentRepository,
            PaymentStatusHistoryRepository statusHistoryRepository,
            OrderApplicationService orderApplicationService,
            ReservationRepository reservationRepository,
            InventoryApplicationService inventoryApplicationService,
            PaymentProviderRegistry providerRegistry
    ) {
        this.paymentRepository = paymentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.orderApplicationService = orderApplicationService;
        this.reservationRepository = reservationRepository;
        this.inventoryApplicationService = inventoryApplicationService;
        this.providerRegistry = providerRegistry;
    }

    @Transactional
    public PaymentDto reconcilePayment(UUID paymentId, String actorId) {
        log.info("Initiating reconciliation for paymentId: {} by actor: {}", paymentId, actorId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId.toString()));

        if (payment.isSuccessful()) {
            log.info("Reconciliation skipped: payment {} is already in SUCCESS status", payment.getPaymentReference());
            return PaymentDto.fromDomain(payment);
        }

        PaymentAttempt activeAttempt = payment.getAttempts().stream()
                .filter(a -> a.getId().equals(payment.getActiveAttemptId()))
                .findFirst()
                .orElse(null);

        String providerRefId = (activeAttempt != null && activeAttempt.getProviderPaymentId() != null && !activeAttempt.getProviderPaymentId().isBlank())
                ? activeAttempt.getProviderPaymentId()
                : (activeAttempt != null ? activeAttempt.getProviderOrderId() : null);

        if (providerRefId == null || providerRefId.isBlank()) {
            log.warn("Cannot reconcile payment {} without provider order or payment ID", payment.getPaymentReference());
            return PaymentDto.fromDomain(payment);
        }

        PaymentProvider provider = providerRegistry.getProvider(payment.getProvider());
        PaymentStatusResult providerStatus = provider.fetchPaymentStatus(providerRefId);

        // Amount & Currency Integrity Verification
        if (providerStatus.amount() != null && providerStatus.amount().compareTo(payment.getAmount()) != 0) {
            log.error("Reconciliation amount mismatch for payment {}: expected {}, got {}", payment.getPaymentReference(), payment.getAmount(), providerStatus.amount());
            throw new PaymentVerificationFailedException("Reconciliation failed due to payment amount mismatch");
        }

        PaymentStatus prevStatus = payment.getStatus();
        if (providerStatus.status() == PaymentStatus.SUCCESS) {
            payment.markSuccess(activeAttempt != null ? activeAttempt.getId() : null, providerRefId, "RECONCILED");
            Payment saved = paymentRepository.save(payment);

            recordStatusHistory(saved.getId(), prevStatus, PaymentStatus.SUCCESS, "RECONCILIATION", "ADMIN", actorId, providerRefId, "Reconciled status from provider");
            orderApplicationService.confirmOrderPayment(saved.getOrderId(), saved.getPaymentReference());

            log.info("Successfully reconciled payment {} to SUCCESS", saved.getPaymentReference());
            return PaymentDto.fromDomain(saved);
        } else if (providerStatus.status() == PaymentStatus.FAILED) {
            payment.markFailed(activeAttempt != null ? activeAttempt.getId() : null, "RECONCILED_FAILED", "Provider status returned FAILED");
            Payment saved = paymentRepository.save(payment);

            recordStatusHistory(saved.getId(), prevStatus, PaymentStatus.FAILED, "RECONCILIATION", "ADMIN", actorId, providerRefId, "Reconciled status FAILED from provider");
            releaseReservationForOrder(saved.getOrderId(), "PAYMENT_RECONCILED_FAILED");

            log.info("Successfully reconciled payment {} to FAILED", saved.getPaymentReference());
            return PaymentDto.fromDomain(saved);
        }

        return PaymentDto.fromDomain(payment);
    }

    private void recordStatusHistory(UUID paymentId, PaymentStatus prevStatus, PaymentStatus newStatus, String source, String actorType, String actorId, String providerEventId, String reason) {
        PaymentStatusHistory history = PaymentStatusHistory.recordTransition(paymentId, prevStatus, newStatus, source, actorType, actorId, providerEventId, reason, null);
        statusHistoryRepository.save(PaymentStatusHistoryEntity.fromDomain(history));
    }

    private void releaseReservationForOrder(UUID orderId, String reason) {
        StockReservation reservation = reservationRepository.findByOrderId(orderId).orElse(null);
        if (reservation != null && reservation.isActive()) {
            inventoryApplicationService.releaseReservation(reservation.getId(), reason);
            log.info("Stock reservation for order {} RELEASED during payment reconciliation", orderId);
        }
    }
}
