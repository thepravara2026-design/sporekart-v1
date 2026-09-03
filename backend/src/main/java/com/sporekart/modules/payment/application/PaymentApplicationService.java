package com.sporekart.modules.payment.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.OrderAccessDeniedException;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.PaymentWebhookEvent;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import com.sporekart.modules.payment.domain.exception.OrderNotPayableException;
import com.sporekart.modules.payment.domain.exception.PaymentInvalidStateException;
import com.sporekart.modules.payment.domain.exception.PaymentNotFoundException;
import com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentAttemptRepository;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.persistence.WebhookEventRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderRequest;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.payment.infrastructure.provider.PaymentVerificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryRepository statusHistoryRepository;
    private final OrderRepository orderRepository;
    private final com.sporekart.modules.order.application.OrderApplicationService orderApplicationService;
    private final ReservationRepository reservationRepository;
    private final InventoryApplicationService inventoryApplicationService;
    private final PaymentProviderRegistry providerRegistry;
    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    public PaymentApplicationService(
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            WebhookEventRepository webhookEventRepository,
            com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryRepository statusHistoryRepository,
            OrderRepository orderRepository,
            com.sporekart.modules.order.application.OrderApplicationService orderApplicationService,
            ReservationRepository reservationRepository,
            InventoryApplicationService inventoryApplicationService,
            PaymentProviderRegistry providerRegistry,
            PaymentProperties paymentProperties,
            ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.orderRepository = orderRepository;
        this.orderApplicationService = orderApplicationService;
        this.reservationRepository = reservationRepository;
        this.inventoryApplicationService = inventoryApplicationService;
        this.providerRegistry = providerRegistry;
        this.paymentProperties = paymentProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentCheckoutDto createPayment(UUID orderId, String customerId) {
        log.info("Initiating payment creation for orderId: {} and customerId: {}", orderId, customerId);

        // 1. Validate Order exists & ownership
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new OrderNotPayableException(orderId, "Order status is " + order.getStatus() + ", must be CREATED");
        }

        // 2. Validate active, non-expired inventory reservation
        StockReservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotPayableException(orderId, "Active inventory reservation required before payment"));

        if (!reservation.isActive() || reservation.isExpired(OffsetDateTime.now())) {
            throw new OrderNotPayableException(orderId, "Inventory reservation is inactive or expired");
        }

        // 3. Find or create Payment aggregate
        PaymentProvider activeProvider = providerRegistry.getActiveProvider();
        PaymentProviderType providerType = activeProvider.getProviderType();

        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderId(orderId);
        Payment payment;

        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();
            if (payment.isSuccessful()) {
                throw new PaymentInvalidStateException("Order " + orderId + " has already been paid successfully");
            }
        } else {
            String payRef = "PAY-" + order.getOrderNumber();
            payment = Payment.createNewPayment(payRef, orderId, customerId, order.getGrandTotal(), order.getCurrency(), providerType);
            payment = paymentRepository.save(payment);
        }

        // 4. Create new PaymentAttempt
        String attRef = payment.getPaymentReference() + "-ATT-" + (payment.getAttempts().size() + 1);
        PaymentAttempt attempt = payment.createAttempt(attRef);

        // 5. Call PaymentProvider to create provider order
        PaymentProviderOrderResult providerResult = activeProvider.createPaymentOrder(
                new PaymentProviderOrderRequest(
                        payment.getPaymentReference(),
                        attRef,
                        orderId,
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getPaymentReference()
                )
        );

        attempt.updateProviderDetails(providerResult.providerOrderId(), providerResult.providerPaymentId(), null);
        Payment saved = paymentRepository.save(payment);

        String keyId = providerType == PaymentProviderType.RAZORPAY ? paymentProperties.getRazorpay().getKeyId() : "mock_key";
        log.info("Successfully created payment attempt {} with providerOrderId {}", attRef, providerResult.providerOrderId());

        return new PaymentCheckoutDto(
                saved.getId(),
                saved.getPaymentReference(),
                attempt.getId(),
                attRef,
                orderId,
                saved.getAmount(),
                saved.getCurrency(),
                providerType,
                providerResult.providerOrderId(),
                keyId
        );
    }

    @Transactional
    public PaymentDto verifyPayment(PaymentVerificationCommand command, String customerId) {
        log.info("Verifying payment callback for paymentRef: {}", command.paymentReference());

        Payment payment = paymentRepository.findByPaymentReference(command.paymentReference())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentReference()));

        if (!payment.getCustomerId().equals(customerId)) {
            throw new OrderAccessDeniedException("Customer does not own this payment record");
        }

        if (payment.isSuccessful()) {
            log.info("Idempotent replay: payment {} is already SUCCESS", payment.getPaymentReference());
            return PaymentDto.fromDomain(payment);
        }

        PaymentProvider provider = providerRegistry.getProvider(payment.getProvider());
        boolean validSig = provider.verifyPaymentSignature(
                new PaymentVerificationRequest(command.providerOrderId(), command.providerPaymentId(), command.providerSignature())
        );

        PaymentAttempt activeAttempt = payment.getAttempts().stream()
                .filter(a -> a.getId().equals(payment.getActiveAttemptId()))
                .findFirst()
                .orElse(null);

        if (!validSig) {
            log.warn("Payment signature verification failed for ref {}", command.paymentReference());
            if (activeAttempt != null) {
                payment.markFailed(activeAttempt.getId(), "INVALID_SIGNATURE", "Cryptographic signature verification failed");
                paymentRepository.save(payment);
            }
            throw new PaymentVerificationFailedException("Payment signature verification failed");
        }

        // Validate provider order ID matching
        if (activeAttempt != null && activeAttempt.getProviderOrderId() != null
                && !activeAttempt.getProviderOrderId().equals(command.providerOrderId())) {
            log.warn("Provider order ID mismatch: expected {}, got {}", activeAttempt.getProviderOrderId(), command.providerOrderId());
            payment.markFailed(activeAttempt.getId(), "ORDER_ID_MISMATCH", "Provider order ID mismatch");
            paymentRepository.save(payment);
            throw new PaymentVerificationFailedException("Provider order ID mismatch");
        }

        // Mark SUCCESS
        PaymentStatus prevStatus = payment.getStatus();
        payment.markSuccess(activeAttempt != null ? activeAttempt.getId() : null, command.providerPaymentId(), command.providerSignature());
        Payment saved = paymentRepository.save(payment);

        statusHistoryRepository.save(com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryEntity.fromDomain(
                com.sporekart.modules.payment.domain.PaymentStatusHistory.recordTransition(
                        saved.getId(), prevStatus, PaymentStatus.SUCCESS, "CLIENT_VERIFY", "CUSTOMER", customerId, command.providerPaymentId(), "Payment signature verified successfully", null
                )
        ));

        // Confirm Order and Reservation
        confirmOrderAndReservation(payment.getOrderId(), payment.getPaymentReference());
        log.info("Successfully verified payment {} for order {}", saved.getPaymentReference(), saved.getOrderId());
        return PaymentDto.fromDomain(saved);
    }

    @Transactional
    public WebhookResponseDto processWebhook(PaymentProviderType providerType, String rawBody, String signatureHeader) {
        log.info("Processing webhook notification for provider {}", providerType);

        PaymentProvider provider = providerRegistry.getProvider(providerType);
        boolean validSig = provider.verifyWebhookSignature(rawBody, signatureHeader);

        ParsedWebhookData parsed = parseWebhookPayload(rawBody);
        String eventId = parsed.eventId();
        if (eventId == null || eventId.isBlank()) {
            eventId = "evt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // Idempotency Check: check if event was already recorded
        Optional<PaymentWebhookEvent> existingOpt = webhookEventRepository.findByProviderAndProviderEventId(providerType, eventId);
        if (existingOpt.isPresent()) {
            PaymentWebhookEvent existing = existingOpt.get();
            if (existing.getProcessingStatus() == WebhookProcessingStatus.PROCESSED) {
                log.info("Webhook event {} already processed, skipping duplicate", eventId);
                return new WebhookResponseDto(WebhookProcessingStatus.DUPLICATE, "Webhook event already processed");
            }
        }

        PaymentWebhookEvent webhookEvent;
        try {
            webhookEvent = webhookEventRepository.save(PaymentWebhookEvent.recordEvent(providerType, eventId, parsed.eventType(), validSig));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("Concurrent duplicate webhook event detected for eventId: {}", eventId);
            return new WebhookResponseDto(WebhookProcessingStatus.DUPLICATE, "Webhook event already being processed or completed");
        }

        if (!validSig) {
            log.warn("Invalid webhook signature for event {}", eventId);
            webhookEvent.markFailed("Invalid signature");
            webhookEventRepository.save(webhookEvent);
            throw new PaymentVerificationFailedException("Webhook signature verification failed");
        }

        // Process trusted webhook event
        if (parsed.providerOrderId() != null) {
            handleTrustedWebhookEvent(providerType, parsed.providerOrderId(), parsed.eventType(), parsed.providerPaymentId(), signatureHeader, eventId);
        }

        webhookEvent.markProcessed();
        webhookEventRepository.save(webhookEvent);
        log.info("Successfully processed webhook event {}", eventId);
        return new WebhookResponseDto(WebhookProcessingStatus.PROCESSED, "Webhook processed successfully");
    }

    private ParsedWebhookData parseWebhookPayload(String rawBody) {
        String eventId = null;
        String eventType = "unknown";
        String providerOrderId = null;
        String providerPaymentId = null;

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            if (root.has("event_id")) {
                eventId = root.get("event_id").asText();
            } else if (root.has("id")) {
                eventId = root.get("id").asText();
            }

            if (root.has("event")) {
                eventType = root.get("event").asText();
            }

            // Extract order_id / payment_id from Razorpay payload node
            if (root.has("payload")) {
                JsonNode payload = root.get("payload");
                if (payload.has("payment") && payload.get("payment").has("entity")) {
                    JsonNode entity = payload.get("payment").get("entity");
                    if (entity.has("order_id")) providerOrderId = entity.get("order_id").asText();
                    if (entity.has("id")) providerPaymentId = entity.get("id").asText();
                } else if (payload.has("order") && payload.get("order").has("entity")) {
                    JsonNode entity = payload.get("order").get("entity");
                    if (entity.has("id")) providerOrderId = entity.get("id").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse webhook JSON body: {}", e.getMessage());
        }

        return new ParsedWebhookData(eventId, eventType, providerOrderId, providerPaymentId);
    }

    private void handleTrustedWebhookEvent(PaymentProviderType providerType, String providerOrderId, String eventType, String providerPaymentId, String signatureHeader, String eventId) {
        Optional<PaymentAttempt> attemptOpt = paymentAttemptRepository.findByProviderAndProviderOrderId(providerType, providerOrderId);
        if (attemptOpt.isEmpty()) {
            return;
        }

        PaymentAttempt attempt = attemptOpt.get();
        Payment payment = paymentRepository.findById(attempt.getPaymentId()).orElse(null);
        if (payment == null || payment.isSuccessful()) {
            return;
        }

        if (eventType.contains("captured") || eventType.contains("authorized") || eventType.contains("success")) {
            PaymentStatus prevStatus = payment.getStatus();
            payment.markSuccess(attempt.getId(), providerPaymentId, signatureHeader);
            Payment saved = paymentRepository.save(payment);
            statusHistoryRepository.save(com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryEntity.fromDomain(
                    com.sporekart.modules.payment.domain.PaymentStatusHistory.recordTransition(
                            saved.getId(), prevStatus, PaymentStatus.SUCCESS, "WEBHOOK", "RAZORPAY", providerType.name(), eventId, "Webhook event " + eventType + " processed", null
                    )
            ));
            confirmOrderAndReservation(saved.getOrderId(), saved.getPaymentReference());
        } else if (eventType.contains("failed")) {
            PaymentStatus prevStatus = payment.getStatus();
            payment.markFailed(attempt.getId(), "WEBHOOK_FAILED", "Payment failed event received from provider");
            Payment saved = paymentRepository.save(payment);
            statusHistoryRepository.save(com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryEntity.fromDomain(
                    com.sporekart.modules.payment.domain.PaymentStatusHistory.recordTransition(
                            saved.getId(), prevStatus, PaymentStatus.FAILED, "WEBHOOK", "RAZORPAY", providerType.name(), eventId, "Webhook event " + eventType + " processed", null
                    )
            ));
            releaseReservationForOrder(saved.getOrderId(), "PAYMENT_FAILED_WEBHOOK");
        }
    }

    private record ParsedWebhookData(String eventId, String eventType, String providerOrderId, String providerPaymentId) {}

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByReference(String paymentReference, String customerId) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException(paymentReference));

        if (!payment.getCustomerId().equals(customerId)) {
            throw new OrderAccessDeniedException("Customer does not own this payment record");
        }
        return PaymentDto.fromDomain(payment);
    }

    private void confirmOrderAndReservation(UUID orderId, String paymentReference) {
        orderApplicationService.confirmOrderPayment(orderId, paymentReference);

        StockReservation reservation = reservationRepository.findByOrderId(orderId).orElse(null);
        if (reservation != null && reservation.isActive()) {
            reservation.confirm();
            reservationRepository.save(reservation);
            log.info("Stock reservation for order {} CONFIRMED", orderId);
        }
    }

    private void releaseReservationForOrder(UUID orderId, String reason) {
        StockReservation reservation = reservationRepository.findByOrderId(orderId).orElse(null);
        if (reservation != null && reservation.isActive()) {
            inventoryApplicationService.releaseReservation(reservation.getId(), reason);
            log.info("Stock reservation for order {} RELEASED due to payment failure", orderId);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void recordPaymentFailure(UUID paymentId, UUID attemptId, String code, String reason) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment != null && !payment.isSuccessful()) {
            payment.markFailed(attemptId, code, reason);
            paymentRepository.save(payment);
        }
    }
}
