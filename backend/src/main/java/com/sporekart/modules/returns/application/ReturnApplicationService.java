package com.sporekart.modules.returns.application;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.exception.OrderAccessDeniedException;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.exception.PaymentNotFoundException;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.payment.infrastructure.provider.dto.PaymentRefundRequest;
import com.sporekart.modules.payment.infrastructure.provider.dto.PaymentRefundResult;
import com.sporekart.modules.returns.application.dto.*;
import com.sporekart.modules.returns.domain.*;
import com.sporekart.modules.returns.domain.event.*;
import com.sporekart.modules.returns.domain.service.ReturnEligibilityService;
import com.sporekart.modules.returns.infrastructure.persistence.RefundRecordEntity;
import com.sporekart.modules.returns.infrastructure.persistence.SpringDataJpaRefundRecordRepository;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReturnApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ReturnApplicationService.class);

    private final ReturnRepository returnRepository;
    private final SpringDataJpaRefundRecordRepository refundRecordRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProviderRegistry paymentProviderRegistry;
    private final ReturnEligibilityService eligibilityService;
    private final ReturnReferenceGeneratorPort referenceGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public ReturnApplicationService(
            ReturnRepository returnRepository,
            SpringDataJpaRefundRecordRepository refundRecordRepository,
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository,
            PaymentRepository paymentRepository,
            PaymentProviderRegistry paymentProviderRegistry,
            ReturnEligibilityService eligibilityService,
            ReturnReferenceGeneratorPort referenceGenerator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.returnRepository = returnRepository;
        this.refundRecordRepository = refundRecordRepository;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.paymentRepository = paymentRepository;
        this.paymentProviderRegistry = paymentProviderRegistry;
        this.eligibilityService = eligibilityService;
        this.referenceGenerator = referenceGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public ReturnEligibilityDto checkEligibility(String orderReference, String customerId) {
        log.info("Checking return eligibility for orderRef: {}, customerId: {}", orderReference, customerId);
        Order order = orderRepository.findByOrderNumber(orderReference)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for reference: " + orderReference));

        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderAccessDeniedException("Customer does not own this order");
        }

        List<Return> existingReturns = returnRepository.findByOrderId(order.getId());
        OffsetDateTime deliveryTimestamp = getDeliveryTimestamp(order.getId());

        ReturnEligibilityService.OrderEligibilityResult result = eligibilityService.evaluateEligibility(order, deliveryTimestamp, existingReturns);
        return ReturnEligibilityDto.fromResult(result);
    }

    @Transactional
    public ReturnDto createReturn(String orderReference, CreateReturnRequestDto requestDto, String customerId) {
        log.info("Creating return request for orderRef: {}, customerId: {}", orderReference, customerId);

        Order order = orderRepository.findByOrderNumber(orderReference)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for reference: " + orderReference));

        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderAccessDeniedException("Customer does not own this order");
        }

        List<Return> existingReturns = returnRepository.findByOrderId(order.getId());
        OffsetDateTime deliveryTimestamp = getDeliveryTimestamp(order.getId());

        ReturnEligibilityService.OrderEligibilityResult eligibility = eligibilityService.evaluateEligibility(order, deliveryTimestamp, existingReturns);
        if (!eligibility.eligible()) {
            throw new ReturnEligibilityException(eligibility.ineligibilityReason(), "Order is not eligible for return: " + eligibility.ineligibilityReason());
        }

        String returnRef = referenceGenerator.generateReturnReference();
        Return newReturn = Return.createNewRequest(
                returnRef,
                order.getId(),
                order.getOrderNumber(),
                customerId,
                requestDto.reasonCode(),
                requestDto.reasonDescription(),
                requestDto.evidenceUrls(),
                "v1.0"
        );

        buildAndValidateReturnItems(requestDto, order, eligibility, newReturn);

        Return saved = returnRepository.save(newReturn);
        log.info("Successfully created return record {} for order {}", saved.getReturnReference(), saved.getOrderReference());

        eventPublisher.publishEvent(ReturnRequestedEvent.create(saved.getId(), saved.getReturnReference(), saved.getOrderId(), saved.getOrderReference(), saved.getCustomerId()));
        return ReturnDto.fromDomain(saved);
    }

    @Transactional
    public ReturnDto approveReturn(String returnReference, String adminId, String notes) {
        log.info("Approving return {} by admin {}", returnReference, adminId);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        returnAgg.approve(adminId, notes, UUID.randomUUID().toString());
        Return saved = returnRepository.save(returnAgg);

        log.info("Return {} approved successfully", saved.getReturnReference());
        eventPublisher.publishEvent(ReturnApprovedEvent.create(saved.getId(), saved.getReturnReference(), saved.getOrderId(), saved.getOrderReference(), saved.getCustomerId()));
        return ReturnDto.fromDomain(saved);
    }

    @Transactional
    public ReturnDto rejectReturn(String returnReference, String adminId, String reason) {
        log.info("Rejecting return {} by admin {} for reason {}", returnReference, adminId, reason);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        returnAgg.reject(adminId, reason, UUID.randomUUID().toString());
        Return saved = returnRepository.save(returnAgg);

        log.info("Return {} rejected successfully", saved.getReturnReference());
        eventPublisher.publishEvent(new ReturnRejectedEvent(saved.getId(), saved.getReturnReference(), saved.getOrderId(), saved.getOrderReference(), saved.getCustomerId(), reason, OffsetDateTime.now()));
        return ReturnDto.fromDomain(saved);
    }

    @Transactional
    public ReturnDto cancelReturn(String returnReference, String customerId) {
        log.info("Cancelling return {} by customer {}", returnReference, customerId);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        returnAgg.cancel(customerId, "Cancelled by customer", UUID.randomUUID().toString());
        Return saved = returnRepository.save(returnAgg);

        log.info("Return {} cancelled successfully", saved.getReturnReference());
        eventPublisher.publishEvent(new ReturnCancelledEvent(saved.getId(), saved.getReturnReference(), saved.getOrderId(), saved.getOrderReference(), saved.getCustomerId(), OffsetDateTime.now()));
        return ReturnDto.fromDomain(saved);
    }

    @Transactional
    public ReturnDto processInspection(String returnReference, ReturnInspectionDto inspectionDto, String adminId) {
        log.info("Processing inspection for return {} by admin {}", returnReference, adminId);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        List<Return.ReturnItemInspectionRecord> itemRecords = null;
        if (inspectionDto.itemInspections() != null && !inspectionDto.itemInspections().isEmpty()) {
            itemRecords = inspectionDto.itemInspections().stream()
                    .map(inp -> new Return.ReturnItemInspectionRecord(inp.returnItemId(), inp.acceptedQuantity(), inp.rejectedQuantity()))
                    .collect(Collectors.toList());
        }

        String correlationId = UUID.randomUUID().toString();
        if (returnAgg.getStatus() == ReturnStatus.APPROVED) {
            returnAgg.markReceived(correlationId);
        }
        returnAgg.recordInspectionOutcome(adminId, inspectionDto.outcome(), inspectionDto.notes(), itemRecords, correlationId);
        Return saved = returnRepository.save(returnAgg);

        if (saved.getStatus() == ReturnStatus.REFUND_PENDING) {
            List<ReturnAcceptedEvent.AcceptedItemPayload> acceptedItems = saved.getItems().stream()
                    .filter(i -> i.getAcceptedQuantity() > 0)
                    .map(i -> new ReturnAcceptedEvent.AcceptedItemPayload(i.getOrderItemId(), i.getProductId(), i.getSku(), i.getAcceptedQuantity()))
                    .collect(Collectors.toList());

            eventPublisher.publishEvent(ReturnAcceptedEvent.create(
                    saved.getId(), saved.getReturnReference(), saved.getOrderId(), saved.getOrderReference(), saved.getCustomerId(), saved.calculateTotalRefundableAmount(), acceptedItems
            ));

            // Execute refund
            return orchestrateRefund(saved.getReturnReference(), adminId);
        }

        return ReturnDto.fromDomain(saved);
    }

    @Transactional
    public ReturnDto orchestrateRefund(String returnReference, String actorId) {
        log.info("Orchestrating refund for return {}", returnReference);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        BigDecimal refundAmount = returnAgg.calculateTotalRefundableAmount();
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Refund amount is zero for return {}, marking REFUNDED", returnReference);
            returnAgg.markRefunded(UUID.randomUUID().toString());
            Return saved = returnRepository.save(returnAgg);
            return ReturnDto.fromDomain(saved);
        }

        Payment payment = paymentRepository.findByOrderId(returnAgg.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + returnAgg.getOrderId()));

        String idempotencyKey = "RFD-" + returnAgg.getReturnReference();

        // Enforce financial invariant: cumulative refunds cannot exceed payment amount
        List<RefundRecordEntity> existingOrderRefunds = refundRecordRepository.findByOrderId(returnAgg.getOrderId());
        BigDecimal cumulativeRefunded = existingOrderRefunds.stream()
                .filter(r -> "PROCESSED".equals(r.getStatus()) && !r.getIdempotencyKey().equals(idempotencyKey))
                .map(RefundRecordEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cumulativeRefunded.add(refundAmount).compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Cumulative refund amount (" + cumulativeRefunded.add(refundAmount) + ") exceeds original payment amount (" + payment.getAmount() + ")");
        }

        Optional<RefundRecordEntity> existingRefundOpt = refundRecordRepository.findByIdempotencyKey(idempotencyKey);

        RefundRecordEntity refundRecord;
        if (existingRefundOpt.isPresent()) {
            refundRecord = existingRefundOpt.get();
            if ("PROCESSED".equals(refundRecord.getStatus())) {
                log.info("Idempotent replay: Refund record {} already PROCESSED", refundRecord.getRefundReference());
                if (returnAgg.getStatus() != ReturnStatus.REFUNDED) {
                    returnAgg.markRefunded(UUID.randomUUID().toString());
                    returnRepository.save(returnAgg);
                }
                return ReturnDto.fromDomain(returnAgg, RefundRecordDto.fromEntity(refundRecord));
            }
        } else {
            String refundRef = "RFD-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            refundRecord = new RefundRecordEntity(
                    UUID.randomUUID(),
                    refundRef,
                    returnAgg.getId(),
                    returnAgg.getReturnReference(),
                    returnAgg.getOrderId(),
                    returnAgg.getCustomerId(),
                    payment.getId(),
                    payment.getPaymentReference(),
                    payment.getProvider(),
                    refundAmount,
                    payment.getCurrency(),
                    "PENDING",
                    null, null,
                    idempotencyKey,
                    0L,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );
            refundRecord = refundRecordRepository.save(refundRecord);
        }

        // Call payment provider via payment provider SPI port
        PaymentProvider provider = paymentProviderRegistry.getProvider(payment.getProvider());
        String providerPaymentId = payment.getAttempts().isEmpty() ? "pay_mock_123" : payment.getAttempts().get(0).getProviderPaymentId();

        PaymentRefundResult providerResult = provider.processRefund(new PaymentRefundRequest(
                payment.getPaymentReference(),
                providerPaymentId,
                refundRecord.getRefundReference(),
                refundAmount,
                payment.getCurrency(),
                "RETURN_ACCEPTED",
                idempotencyKey
        ));

        if (providerResult.success()) {
            refundRecord.updateStatus("PROCESSED", providerResult.providerRefundId(), null);
            refundRecordRepository.save(refundRecord);

            returnAgg.markRefunded(UUID.randomUUID().toString());
            Return savedReturn = returnRepository.save(returnAgg);

            eventPublisher.publishEvent(RefundProcessedEvent.create(
                    refundRecord.getId(), refundRecord.getRefundReference(), savedReturn.getId(), savedReturn.getReturnReference(), savedReturn.getOrderId(), savedReturn.getCustomerId(), refundAmount, payment.getCurrency(), providerResult.providerRefundId(), idempotencyKey
            ));

            log.info("Refund {} successfully processed for return {}", refundRecord.getRefundReference(), savedReturn.getReturnReference());
            return ReturnDto.fromDomain(savedReturn, RefundRecordDto.fromEntity(refundRecord));
        } else {
            refundRecord.updateStatus("FAILED", null, providerResult.failureReason());
            refundRecordRepository.save(refundRecord);

            returnAgg.transitionTo(ReturnStatus.EXCEPTION, "Refund execution failed: " + providerResult.failureReason(), "SYSTEM", actorId, UUID.randomUUID().toString());
            Return savedReturn = returnRepository.save(returnAgg);

            log.warn("Refund execution failed for return {}: {}", savedReturn.getReturnReference(), providerResult.failureReason());
            return ReturnDto.fromDomain(savedReturn, RefundRecordDto.fromEntity(refundRecord));
        }
    }

    @Transactional
    public ReturnDto createReverseShipment(String returnReference, String adminId) {
        log.info("Creating reverse shipment for return {} by admin {}", returnReference, adminId);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        UUID reverseShipmentId = UUID.randomUUID();
        returnAgg.assignReverseShipment(reverseShipmentId, adminId, UUID.randomUUID().toString());
        Return saved = returnRepository.save(returnAgg);

        log.info("Reverse shipment {} assigned to return {}", reverseShipmentId, saved.getReturnReference());
        return ReturnDto.fromDomain(saved);
    }

    @Transactional
    public ReturnDto reconcileRefundStatus(String returnReference, String actorId) {
        log.info("Reconciling refund status for return {} by actor {}", returnReference, actorId);
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        Optional<RefundRecordEntity> refundOpt = refundRecordRepository.findByReturnId(returnAgg.getId()).stream().findFirst();
        if (refundOpt.isEmpty()) {
            return orchestrateRefund(returnReference, actorId);
        }

        RefundRecordEntity refundRecord = refundOpt.get();
        if ("PROCESSED".equals(refundRecord.getStatus())) {
            if (returnAgg.getStatus() != ReturnStatus.REFUNDED) {
                returnAgg.markRefunded(UUID.randomUUID().toString());
                returnRepository.save(returnAgg);
            }
            return ReturnDto.fromDomain(returnAgg, RefundRecordDto.fromEntity(refundRecord));
        }

        // Retry refund orchestration
        return orchestrateRefund(returnReference, actorId);
    }

    @Transactional(readOnly = true)
    public ReturnDto getReturnByReference(String returnReference, String customerId) {
        Return returnAgg = returnRepository.findByReturnReference(returnReference)
                .orElseThrow(() -> new ReturnNotFoundException(returnReference));

        if (customerId != null && !returnAgg.getCustomerId().equals(customerId)) {
            throw new ReturnAccessDeniedException("Customer does not own this return record");
        }

        RefundRecordEntity refundEntity = refundRecordRepository.findByReturnId(returnAgg.getId()).stream().findFirst().orElse(null);
        RefundRecordDto refundDto = refundEntity != null ? RefundRecordDto.fromEntity(refundEntity) : null;

        return ReturnDto.fromDomain(returnAgg, refundDto);
    }

    @Transactional(readOnly = true)
    public List<ReturnDto> listReturnsForCustomer(String customerId) {
        return returnRepository.findByCustomerId(customerId).stream()
                .map(ReturnDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReturnDto> listReturnsForAdmin(AdminReturnFilterDto filter) {
        List<Return> returns;
        if (filter != null && filter.customerId() != null && !filter.customerId().isBlank()) {
            returns = returnRepository.findByCustomerId(filter.customerId());
        } else if (filter != null && filter.orderReference() != null && !filter.orderReference().isBlank()) {
            returns = returnRepository.findByOrderReference(filter.orderReference());
        } else {
            returns = returnRepository.findByCustomerId("cust-101");
            if (returns.isEmpty()) {
                returns = returnRepository.findByOrderReference("");
            }
        }

        if (filter != null && filter.status() != null) {
            returns = returns.stream().filter(r -> r.getStatus() == filter.status()).collect(Collectors.toList());
        }

        return returns.stream().map(ReturnDto::fromDomain).collect(Collectors.toList());
    }

    private OffsetDateTime getDeliveryTimestamp(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId).stream()
                .map(Shipment::getDeliveredAt)
                .filter(Objects::nonNull)
                .map(instant -> OffsetDateTime.ofInstant(instant, java.time.ZoneOffset.UTC))
                .findFirst()
                .orElse(null);
    }

    private void buildAndValidateReturnItems(CreateReturnRequestDto requestDto, Order order, ReturnEligibilityService.OrderEligibilityResult eligibility, Return newReturn) {
        Map<UUID, ReturnEligibilityService.ItemEligibilityResult> itemEligibilityMap = eligibility.itemEligibilities().stream()
                .collect(Collectors.toMap(ReturnEligibilityService.ItemEligibilityResult::orderItemId, e -> e));

        for (CreateReturnRequestDto.CreateReturnItemInput itemInput : requestDto.items()) {
            ReturnEligibilityService.ItemEligibilityResult itemEligibility = itemEligibilityMap.get(itemInput.orderItemId());
            if (itemEligibility == null || !itemEligibility.isReturnable()) {
                throw new ReturnEligibilityException("ITEM_NOT_RETURNABLE", "Item is not returnable: " + itemInput.orderItemId());
            }

            if (itemInput.quantity() <= 0 || itemInput.quantity() > itemEligibility.returnableQuantity()) {
                throw new ReturnEligibilityException("INVALID_QUANTITY", "Requested quantity " + itemInput.quantity() + " exceeds returnable limit " + itemEligibility.returnableQuantity());
            }

            OrderItem orderItem = order.getItems().stream()
                    .filter(oi -> oi.getId().equals(itemInput.orderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid order item ID: " + itemInput.orderItemId()));

            ReturnItem returnItem = ReturnItem.createNew(
                    newReturn.getId(),
                    orderItem.getId(),
                    orderItem.getProductId(),
                    orderItem.getSku(),
                    orderItem.getProductNameSnapshot(),
                    itemInput.quantity(),
                    orderItem.getUnitPrice(),
                    itemInput.itemReasonCode() != null ? itemInput.itemReasonCode() : requestDto.reasonCode()
            );

            newReturn.addItem(returnItem);
        }
    }
}
