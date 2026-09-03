package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentRescheduledEvent;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.InvalidBatchStateException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TrainingRescheduleService {

    private static final Logger log = LoggerFactory.getLogger(TrainingRescheduleService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentHistoryRepository historyRepository;
    private final CapacityApplicationService capacityService;
    private final CancellationEligibilityService eligibilityService;
    private final SecurityAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public TrainingRescheduleService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentHistoryRepository historyRepository,
            CapacityApplicationService capacityService,
            CancellationEligibilityService eligibilityService,
            SecurityAuditService auditService,
            ApplicationEventPublisher eventPublisher) {
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository must not be null");
        this.capacityService = Objects.requireNonNull(capacityService, "capacityService must not be null");
        this.eligibilityService = Objects.requireNonNull(eligibilityService, "eligibilityService must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    }

    @Transactional
    public TrainingEnrollment rescheduleEnrollmentByTrainee(String enrollmentId, String targetBatchId, String reason, String traineeId) {
        if (traineeId == null || traineeId.isBlank() || "ANONYMOUS_TRAINEE".equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required to reschedule training enrollment");
        }

        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Training enrollment not found for id: " + enrollmentId));

        if (!enrollment.getTraineeId().equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: You do not own enrollment " + enrollmentId);
        }

        String sourceBatchId = enrollment.getBatchId();
        if (sourceBatchId.equals(targetBatchId)) {
            log.info("Enrollment id={} is already assigned to batchId={}. Idempotent return.", enrollmentId, targetBatchId);
            return enrollment;
        }

        TrainingBatch sourceBatch = batchRepository.findById(sourceBatchId)
                .orElseThrow(() -> new BatchNotFoundException("Source training batch not found for id: " + sourceBatchId));

        eligibilityService.validateTraineeRescheduleEligibility(sourceBatch);

        TrainingBatch targetBatch = batchRepository.findById(targetBatchId)
                .orElseThrow(() -> new BatchNotFoundException("Target training batch not found for id: " + targetBatchId));

        validateTargetBatchCompatibility(sourceBatch, targetBatch);

        return executeAtomicReschedule(enrollment, sourceBatch, targetBatch, reason, traineeId);
    }

    @Transactional
    public TrainingEnrollment rescheduleEnrollmentByAdmin(String enrollmentId, String targetBatchId, String reason, String adminUser) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Training enrollment not found for id: " + enrollmentId));

        String sourceBatchId = enrollment.getBatchId();
        if (sourceBatchId.equals(targetBatchId)) {
            log.info("Enrollment id={} is already assigned to batchId={}. Idempotent return.", enrollmentId, targetBatchId);
            return enrollment;
        }

        TrainingBatch sourceBatch = batchRepository.findById(sourceBatchId)
                .orElseThrow(() -> new BatchNotFoundException("Source training batch not found for id: " + sourceBatchId));

        eligibilityService.validateAdminRescheduleEligibility(sourceBatch);

        TrainingBatch targetBatch = batchRepository.findById(targetBatchId)
                .orElseThrow(() -> new BatchNotFoundException("Target training batch not found for id: " + targetBatchId));

        validateTargetBatchCompatibility(sourceBatch, targetBatch);

        return executeAtomicReschedule(enrollment, sourceBatch, targetBatch, reason, adminUser);
    }

    private void validateTargetBatchCompatibility(TrainingBatch sourceBatch, TrainingBatch targetBatch) {
        if (targetBatch.getStatus() == BatchStatus.CANCELLED || targetBatch.getStatus() == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Target batch " + targetBatch.getBatchCode() + " is in invalid status " + targetBatch.getStatus() + " for rescheduling");
        }
        if (!targetBatch.getProgramId().equals(sourceBatch.getProgramId())) {
            throw new IllegalArgumentException("Target batch " + targetBatch.getBatchCode() + " belongs to program " + targetBatch.getProgramId() + " which does not match source program " + sourceBatch.getProgramId());
        }
    }

    private TrainingEnrollment executeAtomicReschedule(TrainingEnrollment enrollment, TrainingBatch sourceBatch, TrainingBatch targetBatch, String reason, String actor) {
        boolean wasCapacityConsuming = enrollment.getStatus().isCapacityConsuming();
        EnrollmentStatus oldStatus = enrollment.getStatus();

        // 1. Reserve slot on target batch first (will throw InsufficientCapacityException if full)
        capacityService.allocateSlot(targetBatch.getId());

        // 2. Release slot on source batch if capacity was consumed
        if (wasCapacityConsuming) {
            capacityService.releaseSlot(sourceBatch.getId());
        }

        // 3. Reassign enrollment batch ID and update domain status
        enrollment.rescheduleToBatch(targetBatch.getId());
        TrainingEnrollment saved = enrollmentRepository.save(enrollment);

        // 4. Save history record
        String rescheduleReason = "Rescheduled from " + sourceBatch.getBatchCode() + " to " + targetBatch.getBatchCode() + (reason != null ? ": " + reason : "");
        historyRepository.save(TrainingEnrollmentHistory.record(saved.getId(), oldStatus, EnrollmentStatus.RESCHEDULED, rescheduleReason, actor));

        // 5. Audit event
        auditService.logEvent(
                AuditEventType.SECURITY_SYSTEM_ALERT,
                actor,
                saved.getId(),
                "127.0.0.1",
                "SYSTEM",
                AuditStatus.SUCCESS,
                "Rescheduled enrollment " + saved.getId() + " from batch " + sourceBatch.getBatchCode() + " to " + targetBatch.getBatchCode()
        );

        // 6. Event Publisher
        eventPublisher.publishEvent(new TrainingEnrollmentRescheduledEvent(saved.getId(), sourceBatch.getId(), targetBatch.getId(), saved.getTraineeId()));

        return saved;
    }
}
