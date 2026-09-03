package com.sporekart.modules.training.application;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentAttemptRepository;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderRequest;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.payment.infrastructure.provider.PaymentVerificationRequest;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.controller.dto.TrainingPaymentOrderResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentStatusResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentVerificationRequestDto;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TrainingPaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TrainingPaymentApplicationService.class);

    private final TrainingBatchRepository batchRepository;
    private final TrainingProgramRepository programRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingEnrollmentPaymentRepository trainingPaymentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentProviderRegistry providerRegistry;
    private final PaymentProperties paymentProperties;
    private final EnrollmentApplicationService enrollmentService;
    private final SecurityAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public TrainingPaymentApplicationService(
            TrainingBatchRepository batchRepository,
            TrainingProgramRepository programRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingEnrollmentPaymentRepository trainingPaymentRepository,
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            PaymentProviderRegistry providerRegistry,
            PaymentProperties paymentProperties,
            EnrollmentApplicationService enrollmentService,
            SecurityAuditService auditService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.batchRepository = Objects.requireNonNull(batchRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository);
        this.trainingPaymentRepository = Objects.requireNonNull(trainingPaymentRepository);
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.paymentAttemptRepository = Objects.requireNonNull(paymentAttemptRepository);
        this.providerRegistry = Objects.requireNonNull(providerRegistry);
        this.paymentProperties = Objects.requireNonNull(paymentProperties);
        this.enrollmentService = Objects.requireNonNull(enrollmentService);
        this.auditService = Objects.requireNonNull(auditService);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Transactional(noRollbackFor = {org.springframework.dao.DataIntegrityViolationException.class, BatchFullException.class, InvalidBatchStateException.class})
    public TrainingPaymentOrderResponse initiatePaymentOrder(String batchId, String traineeId) {
        log.info("Initiating training payment order for batchId={} and traineeId={}", batchId, traineeId);

        synchronized ((batchId + ":" + traineeId).intern()) {
            // 1. Validate batch exists and status
            TrainingBatch batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        if (batch.getStatus() == BatchStatus.FULL || batch.getCapacity().isFull()) {
            throw new BatchFullException("Cannot initiate payment: Batch " + batch.getBatchCode() + " is FULL");
        }

        if (batch.getStatus() == BatchStatus.CANCELLED || batch.getStatus() == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Batch " + batch.getBatchCode() + " is in state " + batch.getStatus());
        }

            // 2. Validate trainee is not already confirmed/enrolled
            Optional<TrainingEnrollment> existingEnrCheck = enrollmentRepository.findByBatchIdAndTraineeId(batchId, traineeId);
            if (existingEnrCheck.isPresent() && existingEnrCheck.get().getStatus().isCapacityConsuming()) {
                throw new DuplicateEnrollmentException("Trainee " + traineeId + " is already confirmed/enrolled in batch " + batchId);
            }

        // 3. Fetch authoritative program price
        TrainingProgram program = programRepository.findById(batch.getProgramId())
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + batch.getProgramId()));

        BigDecimal priceAmount = program.getPriceAmount();
        String currency = program.getCurrency() != null ? program.getCurrency() : "INR";

        if (priceAmount == null || priceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentStateException("Training program " + program.getSlug() + " is free (price=0). Direct enrollment should be used.");
        }

        // 4. Idempotency Check: reuse active pending payment if exists
        Optional<TrainingEnrollmentPayment> existingOpt = trainingPaymentRepository
                .findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.PENDING);

        if (existingOpt.isPresent()) {
            TrainingEnrollmentPayment existing = existingOpt.get();
            Optional<Payment> genericPaymentOpt = paymentRepository.findById(UUID.fromString(existing.getPaymentId()));
            if (genericPaymentOpt.isPresent()) {
                Payment genPayment = genericPaymentOpt.get();
                PaymentAttempt activeAttempt = genPayment.getAttempts().stream()
                        .filter(a -> a.getId().equals(genPayment.getActiveAttemptId()))
                        .findFirst()
                        .orElse(null);

                if (activeAttempt != null && activeAttempt.getProviderOrderId() != null) {
                    PaymentProviderType providerType = genPayment.getProvider();
                    String keyId = providerType == PaymentProviderType.RAZORPAY ? paymentProperties.getRazorpay().getKeyId() : "mock_key";
                    log.info("Reusing existing pending payment order id={} for batchId={}, traineeId={}", existing.getId(), batchId, traineeId);
                    return new TrainingPaymentOrderResponse(
                            existing.getId(),
                            genPayment.getId().toString(),
                            genPayment.getPaymentReference(),
                            batchId,
                            genPayment.getAmount(),
                            genPayment.getCurrency(),
                            providerType,
                            activeAttempt.getProviderOrderId(),
                            keyId
                    );
                }
            }
        }

        // 5. Create Payment aggregate & Attempt using Provider Abstraction
        PaymentProvider activeProvider = providerRegistry.getActiveProvider();
        PaymentProviderType providerType = activeProvider.getProviderType();

        UUID paymentUuid = UUID.randomUUID();
        String paymentRef = "TRN-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        UUID orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        Payment payment = Payment.createNewPayment(paymentRef, orderUuid, traineeId, priceAmount, currency, providerType);
        Payment savedPayment = paymentRepository.save(payment);

        String attRef = paymentRef + "-ATT-1";
        PaymentAttempt attempt = savedPayment.createAttempt(attRef);

        PaymentProviderOrderResult providerResult = activeProvider.createPaymentOrder(
                new PaymentProviderOrderRequest(paymentRef, attRef, orderUuid, priceAmount, currency, paymentRef)
        );

        attempt.updateProviderDetails(providerResult.providerOrderId(), providerResult.providerPaymentId(), null);
        paymentRepository.save(savedPayment);

        // 6. Save TrainingEnrollmentPayment record & set enrollment status to PAYMENT_PENDING
        Optional<TrainingEnrollment> existingEnrollmentOpt = enrollmentRepository.findByBatchIdAndTraineeId(batchId, traineeId);
        if (existingEnrollmentOpt.isPresent()) {
            TrainingEnrollment existingEnr = existingEnrollmentOpt.get();
            if (existingEnr.getStatus() == com.sporekart.modules.training.domain.EnrollmentStatus.PENDING) {
                existingEnr.markPaymentPending();
                enrollmentRepository.save(existingEnr);
            }
        } else {
            TrainingEnrollment newEnr = TrainingEnrollment.create(batchId, traineeId, priceAmount, currency, null, traineeId);
            newEnr.markPaymentPending();
            enrollmentRepository.save(newEnr);
        }

        TrainingEnrollmentPayment trnPayment = TrainingEnrollmentPayment.create(
                savedPayment.getId().toString(),
                batchId,
                traineeId,
                priceAmount,
                currency,
                traineeId
        );

        TrainingEnrollmentPayment savedTrnPayment;
        try {
            savedTrnPayment = trainingPaymentRepository.save(trnPayment);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            log.info("Concurrent payment order creation detected for batchId={}, traineeId={}. Re-fetching pending payment.", batchId, traineeId);
            Optional<TrainingEnrollmentPayment> retryOpt = trainingPaymentRepository
                    .findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.PENDING);

            if (retryOpt.isEmpty()) {
                for (int retry = 0; retry < 5; retry++) {
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                    retryOpt = trainingPaymentRepository
                            .findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.PENDING);
                    if (retryOpt.isPresent()) break;
                }
            }
            if (retryOpt.isPresent()) {
                TrainingEnrollmentPayment existing = retryOpt.get();
                Optional<Payment> genericPaymentOpt = paymentRepository.findById(UUID.fromString(existing.getPaymentId()));
                if (genericPaymentOpt.isPresent()) {
                    Payment genPayment = genericPaymentOpt.get();
                    PaymentAttempt activeAttempt = genPayment.getAttempts().stream()
                            .filter(a -> a.getId().equals(genPayment.getActiveAttemptId()))
                            .findFirst()
                            .orElse(null);

                    if (activeAttempt != null && activeAttempt.getProviderOrderId() != null) {
                        String keyId = providerType == PaymentProviderType.RAZORPAY ? paymentProperties.getRazorpay().getKeyId() : "mock_key";
                        return new TrainingPaymentOrderResponse(
                                existing.getId(),
                                genPayment.getId().toString(),
                                genPayment.getPaymentReference(),
                                batchId,
                                genPayment.getAmount(),
                                genPayment.getCurrency(),
                                providerType,
                                activeAttempt.getProviderOrderId(),
                                keyId
                        );
                    }
                }
            }
            throw ex;
        }

        String keyId = providerType == PaymentProviderType.RAZORPAY ? paymentProperties.getRazorpay().getKeyId() : "mock_key";

        auditService.logEvent(
                AuditEventType.SECURITY_SYSTEM_ALERT,
                traineeId,
                savedTrnPayment.getId(),
                "127.0.0.1",
                "TrainingModule",
                AuditStatus.SUCCESS,
                "Created training payment order ref=" + paymentRef + " amount=" + priceAmount + " currency=" + currency
        );

        return new TrainingPaymentOrderResponse(
                savedTrnPayment.getId(),
                savedPayment.getId().toString(),
                paymentRef,
                batchId,
                priceAmount,
                currency,
                providerType,
                providerResult.providerOrderId(),
                keyId
        );
        }
    }

    @Transactional(noRollbackFor = {BatchFullException.class, InvalidBatchStateException.class})
    public TrainingPaymentStatusResponse verifyPayment(String batchId, TrainingPaymentVerificationRequestDto command, String traineeId) {
        log.info("Verifying training payment for paymentRef={} on batchId={} by traineeId={}", command.paymentReference(), batchId, traineeId);

        // 1. Find generic Payment by reference
        Payment payment = paymentRepository.findByPaymentReference(command.paymentReference())
                .orElseThrow(() -> new PaymentVerificationException("Payment record not found for reference: " + command.paymentReference()));

        if (!payment.getCustomerId().equalsIgnoreCase(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Trainee " + traineeId + " does not own payment reference " + command.paymentReference());
        }

        // 2. Find TrainingEnrollmentPayment
        TrainingEnrollmentPayment trnPayment = trainingPaymentRepository.findByPaymentId(payment.getId().toString())
                .orElseThrow(() -> new PaymentVerificationException("Training payment context not found for paymentId: " + payment.getId()));

        if (!trnPayment.getBatchId().equals(batchId)) {
            throw new PaymentVerificationException("Payment batchId mismatch: expected " + trnPayment.getBatchId() + ", got " + batchId);
        }

        // Idempotency check: if already confirmed, return current state
        if (trnPayment.getStatus() == TrainingPaymentStatus.ENROLLMENT_CONFIRMED) {
            log.info("Idempotent replay: training payment {} already ENROLLMENT_CONFIRMED", trnPayment.getId());
            return toStatusResponse(trnPayment);
        }

        // 3. Verify signature with PaymentProvider
        PaymentProvider provider = providerRegistry.getProvider(payment.getProvider());
        boolean validSig = provider.verifyPaymentSignature(
                new PaymentVerificationRequest(command.providerOrderId(), command.providerPaymentId(), command.providerSignature())
        );

        PaymentAttempt activeAttempt = payment.getAttempts().stream()
                .filter(a -> a.getId().equals(payment.getActiveAttemptId()))
                .findFirst()
                .orElse(null);

        if (!validSig) {
            log.warn("Invalid payment signature for training payment ref={}", command.paymentReference());
            if (activeAttempt != null) {
                payment.markFailed(activeAttempt.getId(), "INVALID_SIGNATURE", "Signature verification failed");
                paymentRepository.save(payment);
            }
            trnPayment.markFailed(traineeId);
            trainingPaymentRepository.save(trnPayment);
            throw new PaymentVerificationException("Payment signature verification failed");
        }

        // 4. Mark Payment SUCCESS & TrainingPayment VERIFIED
        payment.markSuccess(activeAttempt != null ? activeAttempt.getId() : null, command.providerPaymentId(), command.providerSignature());
        paymentRepository.save(payment);

        trnPayment.markVerified(traineeId);
        trnPayment = trainingPaymentRepository.save(trnPayment);

        // 5. Attempt atomic enrollment confirmation & capacity slot allocation
        try {
            TrainingEnrollment enrollment = enrollmentService.enrollTrainee(batchId, traineeId, "PAYMENT-" + command.paymentReference());
            trnPayment.markEnrollmentConfirmed(enrollment.getId(), traineeId);
            trnPayment = trainingPaymentRepository.save(trnPayment);
            log.info("Successfully confirmed paid enrollment id={} for training payment id={}", enrollment.getId(), trnPayment.getId());
        } catch (BatchFullException | InvalidBatchStateException ex) {
            // CAPACITY RACE CONDITION: Payment captured but batch ran out of seats before verification!
            // GUARANTEE: NEVER OVERBOOK BATCH. Mark ENROLLMENT_PENDING for reconciliation/refund path.
            log.error("CAPACITY RACE DETECTED: Training payment id={} verified, but batchId={} is FULL/Closed ({})", trnPayment.getId(), batchId, ex.getMessage());
            trnPayment.markEnrollmentPending(traineeId);
            trnPayment = trainingPaymentRepository.save(trnPayment);

            auditService.logEvent(
                    AuditEventType.SECURITY_SYSTEM_ALERT,
                    traineeId,
                    trnPayment.getId(),
                    "127.0.0.1",
                    "TrainingModule",
                    AuditStatus.FAILURE,
                    "PAID_ENROLLMENT_PENDING_CAPACITY_FULL: payment verified but batch " + batchId + " capacity ran out"
            );
        }

        return toStatusResponse(trnPayment);
    }

    @Transactional(readOnly = true)
    public TrainingPaymentStatusResponse getPaymentStatus(String batchId, String traineeId) {
        TrainingEnrollmentPayment trnPayment = trainingPaymentRepository
                .findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.ENROLLMENT_CONFIRMED)
                .or(() -> trainingPaymentRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.VERIFIED))
                .or(() -> trainingPaymentRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.PENDING))
                .or(() -> trainingPaymentRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, TrainingPaymentStatus.ENROLLMENT_PENDING))
                .orElseThrow(() -> new PaymentVerificationException("No training payment found for batchId " + batchId + " and trainee " + traineeId));

        return toStatusResponse(trnPayment);
    }

    private TrainingPaymentStatusResponse toStatusResponse(TrainingEnrollmentPayment p) {
        return new TrainingPaymentStatusResponse(
                p.getId(),
                p.getPaymentId(),
                p.getBatchId(),
                p.getTraineeId(),
                p.getEnrollmentId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
