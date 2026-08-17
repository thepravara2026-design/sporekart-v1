package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TrainingCancellationService {

    private static final Logger log = LoggerFactory.getLogger(TrainingCancellationService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingBatchRepository batchRepository;
    private final EnrollmentLifecycleService lifecycleService;
    private final CapacityApplicationService capacityService;
    private final CancellationEligibilityService eligibilityService;

    public TrainingCancellationService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingBatchRepository batchRepository,
            EnrollmentLifecycleService lifecycleService,
            CapacityApplicationService capacityService,
            CancellationEligibilityService eligibilityService) {
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.lifecycleService = Objects.requireNonNull(lifecycleService, "lifecycleService must not be null");
        this.capacityService = Objects.requireNonNull(capacityService, "capacityService must not be null");
        this.eligibilityService = Objects.requireNonNull(eligibilityService, "eligibilityService must not be null");
    }

    @Transactional
    public TrainingEnrollment cancelEnrollmentByTrainee(String enrollmentId, String reason, String traineeId) {
        if (traineeId == null || traineeId.isBlank() || "ANONYMOUS_TRAINEE".equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required to cancel training enrollment");
        }

        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Training enrollment not found for id: " + enrollmentId));

        if (!enrollment.getTraineeId().equals(traineeId)) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: You do not own enrollment " + enrollmentId);
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            log.info("Enrollment id={} is already CANCELLED. Idempotent return.", enrollmentId);
            return enrollment;
        }

        TrainingBatch batch = batchRepository.findById(enrollment.getBatchId())
                .orElseThrow(() -> new BatchNotFoundException("Training batch not found for id: " + enrollment.getBatchId()));

        eligibilityService.validateTraineeCancellationEligibility(batch);

        boolean wasCapacityConsuming = enrollment.getStatus().isCapacityConsuming();

        String cancellationReason = reason != null && !reason.isBlank() ? reason : "Cancelled by trainee";
        TrainingEnrollment cancelled = lifecycleService.transitionStatus(enrollmentId, EnrollmentStatus.CANCELLED, cancellationReason, traineeId);

        if (wasCapacityConsuming) {
            capacityService.releaseSlot(batch.getId());
        }

        return cancelled;
    }

    @Transactional
    public TrainingEnrollment cancelEnrollmentByAdmin(String enrollmentId, String reason, String adminUser) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Training enrollment not found for id: " + enrollmentId));

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            log.info("Enrollment id={} is already CANCELLED. Idempotent return.", enrollmentId);
            return enrollment;
        }

        TrainingBatch batch = batchRepository.findById(enrollment.getBatchId())
                .orElseThrow(() -> new BatchNotFoundException("Training batch not found for id: " + enrollment.getBatchId()));

        eligibilityService.validateAdminCancellationEligibility(batch);

        boolean wasCapacityConsuming = enrollment.getStatus().isCapacityConsuming();

        String cancellationReason = reason != null && !reason.isBlank() ? reason : "Cancelled by administrator";
        TrainingEnrollment cancelled = lifecycleService.transitionStatus(enrollmentId, EnrollmentStatus.CANCELLED, cancellationReason, adminUser);

        if (wasCapacityConsuming) {
            capacityService.releaseSlot(batch.getId());
        }

        return cancelled;
    }
}
