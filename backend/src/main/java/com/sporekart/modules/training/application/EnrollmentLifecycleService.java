package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.BatchBecameFullEvent;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentConfirmedEvent;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentStatusChangedEvent;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EnrollmentLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentLifecycleService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingEnrollmentHistoryRepository historyRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentPaymentRepository paymentRepository;
    private final TrainingDemandRepository demandRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityAuditService auditService;

    @Autowired
    public EnrollmentLifecycleService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingEnrollmentHistoryRepository historyRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentPaymentRepository paymentRepository,
            @Autowired(required = false) TrainingDemandRepository demandRepository,
            ApplicationEventPublisher eventPublisher,
            SecurityAuditService auditService) {
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository must not be null");
        this.demandRepository = demandRepository;
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    @Transactional(noRollbackFor = {BatchFullException.class, InvalidEnrollmentStateException.class})
    public TrainingEnrollment transitionStatus(String enrollmentId, EnrollmentStatus newStatus, String reason, String actor) {
        Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        Objects.requireNonNull(newStatus, "newStatus must not be null");

        synchronized (enrollmentId.intern()) {
            TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new EnrollmentNotFoundException("TrainingEnrollment not found for id: " + enrollmentId));

            EnrollmentStatus oldStatus = enrollment.getStatus();
            if (oldStatus == newStatus) {
                log.info("No-op transition requested for enrollment id={} already in status={}", enrollmentId, newStatus);
                return enrollment;
            }

            // Validate transition
            if (!oldStatus.isValidTransitionTo(newStatus)) {
                throw new InvalidEnrollmentStateException("Illegal status transition from " + oldStatus + " to " + newStatus + " for enrollment id=" + enrollmentId);
            }

        // If transitioning to CONFIRMED, execute atomic seat allocation if not previously allocated
        if (newStatus == EnrollmentStatus.CONFIRMED && !oldStatus.isCapacityConsuming()) {
            boolean seatAllocated = batchRepository.tryAllocateSeatAtomic(enrollment.getBatchId());
            if (!seatAllocated) {
                throw new BatchFullException("Capacity allocation failed: Batch is FULL for enrollment id=" + enrollmentId);
            }
            enrollment.confirm(enrollment.getPaymentReference());
        } else {
            // Apply other state transitions
            switch (newStatus) {
                case PAYMENT_PENDING -> enrollment.markPaymentPending();
                case PAYMENT_VERIFIED -> enrollment.markPaymentVerified(enrollment.getPaymentReference());
                case ACTIVE -> enrollment.activate();
                case COMPLETED -> enrollment.complete();
                case REJECTED -> enrollment.reject(reason);
                case PAYMENT_FAILED -> enrollment.markPaymentFailed();
                case WAITLISTED -> enrollment.waitlist();
                case CANCELLED -> enrollment.cancel();
                default -> throw new InvalidEnrollmentStateException("Unsupported status transition target: " + newStatus);
            }
        }

        TrainingEnrollment saved = enrollmentRepository.save(enrollment);

        // Record history entry
        TrainingEnrollmentHistory history = TrainingEnrollmentHistory.record(enrollmentId, oldStatus, newStatus, reason, actor != null ? actor : "SYSTEM");
        historyRepository.save(history);

        // Resolve active demand if confirmed
        if (newStatus == EnrollmentStatus.CONFIRMED && demandRepository != null) {
            demandRepository.findByBatchIdAndTraineeIdAndStatus(saved.getBatchId(), saved.getTraineeId(), DemandStatus.ACTIVE)
                    .ifPresent(demand -> {
                        demand.resolve(actor != null ? actor : saved.getTraineeId());
                        demandRepository.save(demand);
                        log.info("Resolved active demand id={} for trainee={} upon confirmation", demand.getId(), saved.getTraineeId());
                    });
        }

        // Publish events
        eventPublisher.publishEvent(new TrainingEnrollmentStatusChangedEvent(saved.getId(), saved.getBatchId(), saved.getTraineeId(), oldStatus, newStatus));
        if (newStatus == EnrollmentStatus.CONFIRMED) {
            eventPublisher.publishEvent(new TrainingEnrollmentConfirmedEvent(saved.getId(), saved.getBatchId(), saved.getTraineeId(), saved.getPaymentReference()));

            TrainingBatch batch = batchRepository.findById(saved.getBatchId()).orElse(null);
            if (batch != null && batch.getStatus() == BatchStatus.FULL) {
                eventPublisher.publishEvent(new BatchBecameFullEvent(batch.getId(), batch.getBatchCode(), batch.getCapacity().getTotalCapacity()));
            }
        }

        auditService.logEvent(
                AuditEventType.SECURITY_SYSTEM_ALERT,
                actor != null ? actor : "SYSTEM",
                saved.getId(),
                "127.0.0.1",
                "SYSTEM",
                AuditStatus.SUCCESS,
                "Transitioned enrollment " + saved.getId() + " from " + oldStatus + " to " + newStatus + " (Reason: " + reason + ")"
        );

        return saved;
        }
    }

    @Transactional(noRollbackFor = {BatchFullException.class})
    public TrainingEnrollment recoverEnrollmentConfirmation(String batchId, String traineeId, String paymentReference, String actor) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        Objects.requireNonNull(traineeId, "traineeId must not be null");

        // Locate existing enrollment
        Optional<TrainingEnrollment> existingOpt = enrollmentRepository.findByBatchIdAndTraineeId(batchId, traineeId);
        if (existingOpt.isEmpty()) {
            throw new EnrollmentNotFoundException("No enrollment found for recovery on batch=" + batchId + ", trainee=" + traineeId);
        }

        TrainingEnrollment enrollment = existingOpt.get();

        // If already confirmed, return idempotently
        if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            log.info("Recovery invoked on already CONFIRMED enrollment id={} for batchId={}, returning idempotently", enrollment.getId(), batchId);
            return enrollment;
        }

        // Locate corresponding payment mapping
        Optional<TrainingEnrollmentPayment> trnPayOpt = paymentRepository.findByBatchIdAndTraineeId(batchId, traineeId);
        if (trnPayOpt.isEmpty()) {
            throw new EnrollmentNotFoundException("No payment record found for recovery on batch=" + batchId + ", trainee=" + traineeId);
        }

        TrainingEnrollmentPayment trnPay = trnPayOpt.get();
        String effectivePayRef = paymentReference != null ? paymentReference : (enrollment.getPaymentReference() != null ? enrollment.getPaymentReference() : trnPay.getPaymentId());

        // Check if payment is verified
        if (trnPay.getStatus() != TrainingPaymentStatus.VERIFIED && trnPay.getStatus() != TrainingPaymentStatus.ENROLLMENT_PENDING) {
            log.warn("Recovery attempted on payment id={} in status={}, skipping confirmation", trnPay.getId(), trnPay.getStatus());
            return enrollment;
        }

        // Attempt atomic capacity allocation
        boolean allocated = batchRepository.tryAllocateSeatAtomic(batchId);
        if (allocated) {
            // Confirm enrollment
            EnrollmentStatus oldStatus = enrollment.getStatus();
            enrollment.confirm(effectivePayRef);
            TrainingEnrollment saved = enrollmentRepository.save(enrollment);

            // Update payment state to ENROLLMENT_CONFIRMED
            trnPay.markEnrollmentConfirmed(saved.getId(), actor);
            paymentRepository.save(trnPay);

            // Record history
            historyRepository.save(TrainingEnrollmentHistory.record(saved.getId(), oldStatus, EnrollmentStatus.CONFIRMED, "RECOVERY_SUCCESS", actor != null ? actor : "SYSTEM"));

            // Resolve demand
            if (demandRepository != null) {
                demandRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, DemandStatus.ACTIVE)
                        .ifPresent(demand -> {
                            demand.resolve(actor != null ? actor : traineeId);
                            demandRepository.save(demand);
                        });
            }

            // Publish events & audit log
            eventPublisher.publishEvent(new TrainingEnrollmentConfirmedEvent(saved.getId(), batchId, traineeId, effectivePayRef));
            eventPublisher.publishEvent(new TrainingEnrollmentStatusChangedEvent(saved.getId(), batchId, traineeId, oldStatus, EnrollmentStatus.CONFIRMED));

            log.info("Recovery successfully confirmed enrollment id={} for trainee={} in batch={}", saved.getId(), traineeId, batchId);
            return saved;
        } else {
            log.warn("Recovery failed: BatchId={} is FULL. Enrollment id={} remains in status={}", batchId, enrollment.getId(), enrollment.getStatus());
            return enrollment;
        }
    }

    @Transactional(readOnly = true)
    public List<TrainingEnrollmentHistory> getEnrollmentHistory(String enrollmentId, String requesterId, boolean isAdmin) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("TrainingEnrollment not found for id: " + enrollmentId));

        if (!isAdmin && !enrollment.getTraineeId().equalsIgnoreCase(requesterId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: Trainee " + requesterId + " cannot view history for enrollment " + enrollmentId);
        }

        return historyRepository.findByEnrollmentIdOrderByCreatedAtAsc(enrollmentId);
    }
}
