package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.event.TrainingDemandCreatedEvent;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.DemandNotFoundException;
import com.sporekart.modules.training.domain.exception.DuplicateDemandException;
import com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException;
import com.sporekart.modules.training.domain.exception.InvalidBatchStateException;
import com.sporekart.modules.training.domain.exception.InvalidDemandStateException;
import com.sporekart.modules.training.domain.exception.UnauthorizedDemandAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DemandApplicationService.class);

    private final TrainingDemandRepository demandRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final SecurityAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public DemandApplicationService(
            TrainingDemandRepository demandRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            SecurityAuditService auditService,
            ApplicationEventPublisher eventPublisher) {
        this.demandRepository = demandRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TrainingDemandRequest createDemand(String batchId, String traineeId) {
        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("Training batch not found: " + batchId));

        if (batch.getStatus() == BatchStatus.CANCELLED || batch.getStatus() == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Cannot request demand for batch in status: " + batch.getStatus());
        }

        // Check if capacity is available - if available, redirect trainee to enrollment
        if (!batch.getCapacity().isFull()) {
            throw new InvalidDemandStateException("Training batch has available capacity. Please proceed with enrollment.");
        }

        // Mutual Exclusivity: Trainee cannot have active enrollment and active demand for same batch
        Optional<TrainingEnrollment> existingEnrollment = enrollmentRepository.findByBatchIdAndTraineeId(batchId, traineeId);
        if (existingEnrollment.isPresent() && existingEnrollment.get().getStatus() != EnrollmentStatus.CANCELLED) {
            throw new DuplicateEnrollmentException("Trainee " + traineeId + " already has an active enrollment in batch " + batchId);
        }

        // Single Active Demand Invariant: Check if active demand already exists
        if (demandRepository.existsByBatchIdAndTraineeIdAndStatus(batchId, traineeId, DemandStatus.ACTIVE)) {
            throw new DuplicateDemandException("Active demand request already exists for trainee " + traineeId + " in batch " + batchId);
        }

        // Create demand (INVARIANT: DEMAND NEVER CONSUMES CAPACITY)
        TrainingDemandRequest demand = TrainingDemandRequest.create(batchId, traineeId, traineeId);
        TrainingDemandRequest saved;
        try {
            saved = demandRepository.save(demand);
        } catch (Exception ex) {
            if (ex instanceof DuplicateDemandException) {
                throw (DuplicateDemandException) ex;
            }
            if (ex instanceof org.springframework.dao.DataIntegrityViolationException
                    || ex instanceof org.springframework.transaction.TransactionSystemException
                    || ex instanceof org.springframework.orm.jpa.JpaSystemException
                    || (ex.getMessage() != null && (ex.getMessage().contains("UQ_") || ex.getMessage().contains("Constraint") || ex.getMessage().contains("duplicate")))) {
                throw new DuplicateDemandException("Active demand request already exists for trainee " + traineeId + " in batch " + batchId);
            }
            throw new RuntimeException("Failed to save demand request", ex);
        }

        eventPublisher.publishEvent(new TrainingDemandCreatedEvent(saved.getId(), batchId, traineeId, saved.getRequestedAt()));

        auditService.logEvent(
                AuditEventType.SECURITY_SYSTEM_ALERT,
                traineeId,
                saved.getId(),
                null,
                null,
                AuditStatus.SUCCESS,
                "DEMAND_CREATED: batch=" + batchId
        );

        log.info("Created demand request id={} for trainee={} in batch={}", saved.getId(), traineeId, batchId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<TrainingDemandRequest> getTraineeDemands(String traineeId, Pageable pageable) {
        return demandRepository.findByTraineeId(traineeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TrainingDemandRequest> getBatchDemandsForAdmin(String batchId, Pageable pageable) {
        return demandRepository.findByBatchId(batchId, pageable);
    }

    @Transactional(readOnly = true)
    public long getBatchDemandCount(String batchId) {
        return demandRepository.countByBatchIdAndStatus(batchId, DemandStatus.ACTIVE);
    }

    @Transactional
    public TrainingDemandRequest withdrawDemand(String demandId, String requesterId, boolean isAdmin) {
        TrainingDemandRequest demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException("Demand request not found: " + demandId));

        if (!isAdmin && !demand.getTraineeId().equalsIgnoreCase(requesterId)) {
            throw new UnauthorizedDemandAccessException("Access denied to demand request: " + demandId);
        }

        demand.withdraw(requesterId);
        return demandRepository.save(demand);
    }

    @Transactional
    public void resolveActiveDemandIfPresent(String batchId, String traineeId, String actor) {
        Optional<TrainingDemandRequest> activeDemand = demandRepository.findByBatchIdAndTraineeIdAndStatus(batchId, traineeId, DemandStatus.ACTIVE);
        if (activeDemand.isPresent()) {
            TrainingDemandRequest demand = activeDemand.get();
            demand.resolve(actor);
            demandRepository.save(demand);
            log.info("Resolved active demand id={} for trainee={} upon enrollment in batch={}", demand.getId(), traineeId, batchId);
        }
    }
}
