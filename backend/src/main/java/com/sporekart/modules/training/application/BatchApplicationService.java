package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.domain.BatchSchedule;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.event.BatchActivatedEvent;
import com.sporekart.modules.training.domain.event.BatchCancelledEvent;
import com.sporekart.modules.training.domain.event.BatchDeactivatedEvent;
import com.sporekart.modules.training.domain.event.BatchScheduledEvent;
import com.sporekart.modules.training.domain.event.BatchUpdatedEvent;
import com.sporekart.modules.training.domain.event.TrainingBatchCreatedEvent;
import com.sporekart.modules.training.domain.exception.BatchAlreadyExistsException;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.BatchReassignmentNotAllowedException;
import com.sporekart.modules.training.domain.exception.TrainingNotFoundException;
import com.sporekart.modules.training.domain.exception.TrainingProgramNotEligibleException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BatchApplicationService {

    private static final Logger log = LoggerFactory.getLogger(BatchApplicationService.class);

    private final TrainingBatchRepository batchRepository;
    private final TrainingProgramRepository programRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityAuditService auditService;

    public BatchApplicationService(TrainingBatchRepository batchRepository,
                                  TrainingProgramRepository programRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  SecurityAuditService auditService) {
        this.batchRepository = batchRepository;
        this.programRepository = programRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    @Transactional
    public TrainingBatch createBatch(String programId, String batchCode, Instant startDate, Instant endDate,
                                    int totalCapacity, DeliveryMode deliveryMode, String venueInfo,
                                    String meetingUrl, String timezone, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";

        TrainingProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + programId));

        if (program.getStatus() == ProgramStatus.INACTIVE || program.getStatus() == ProgramStatus.ARCHIVED) {
            throw new TrainingProgramNotEligibleException("Cannot create batch for training program in status: " + program.getStatus());
        }

        if (batchRepository.findByBatchCode(batchCode).isPresent()) {
            throw new BatchAlreadyExistsException("Batch with code '" + batchCode + "' already exists");
        }

        TrainingBatch batch = TrainingBatch.create(
                program.getId(),
                batchCode,
                startDate,
                endDate,
                totalCapacity,
                deliveryMode,
                venueInfo,
                meetingUrl,
                timezone,
                effectiveActor
        );

        TrainingBatch saved = batchRepository.save(batch);

        eventPublisher.publishEvent(new TrainingBatchCreatedEvent(saved.getId(), saved.getProgramId(), saved.getBatchCode(), totalCapacity));
        eventPublisher.publishEvent(new BatchScheduledEvent(saved.getId(), saved.getProgramId(), saved.getBatchCode(), saved.getStartDate(), saved.getEndDate()));

        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Created Batch code='" + saved.getBatchCode() + "'");

        log.info("Created TrainingBatch id={}, batchCode='{}', programId='{}'", saved.getId(), saved.getBatchCode(), saved.getProgramId());
        return saved;
    }

    @Transactional
    public TrainingBatch updateBatch(String batchId, String targetProgramId, Instant startDate, Instant endDate,
                                    String timezone, DeliveryMode deliveryMode, String venueInfo,
                                    String meetingUrl, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        if (targetProgramId != null && !targetProgramId.isBlank() && !targetProgramId.equals(batch.getProgramId())) {
            throw new BatchReassignmentNotAllowedException("Reassignment of TrainingProgramId for an existing batch is prohibited");
        }

        batch.updateSchedule(startDate, endDate, timezone, deliveryMode, venueInfo, meetingUrl, effectiveActor);
        TrainingBatch updated = batchRepository.save(batch);

        eventPublisher.publishEvent(new BatchUpdatedEvent(updated.getId(), updated.getBatchCode()));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, updated.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Updated Batch code='" + updated.getBatchCode() + "'");

        log.info("Updated TrainingBatch id={}, batchCode='{}'", updated.getId(), updated.getBatchCode());
        return updated;
    }

    @Transactional
    public TrainingBatch activateBatch(String batchId, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        batch.activate();
        TrainingBatch saved = batchRepository.save(batch);

        eventPublisher.publishEvent(new BatchActivatedEvent(saved.getId(), saved.getBatchCode()));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Activated Batch id=" + saved.getId());

        log.info("Activated TrainingBatch id={}", saved.getId());
        return saved;
    }

    @Transactional
    public TrainingBatch deactivateBatch(String batchId, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        batch.deactivate();
        TrainingBatch saved = batchRepository.save(batch);

        eventPublisher.publishEvent(new BatchDeactivatedEvent(saved.getId(), saved.getBatchCode()));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Deactivated Batch id=" + saved.getId());

        log.info("Deactivated TrainingBatch id={}", saved.getId());
        return saved;
    }

    @Transactional
    public TrainingBatch cancelBatch(String batchId, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        batch.cancel();
        TrainingBatch saved = batchRepository.save(batch);

        eventPublisher.publishEvent(new BatchCancelledEvent(saved.getId(), saved.getBatchCode()));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Cancelled Batch id=" + saved.getId());

        log.info("Cancelled TrainingBatch id={}", saved.getId());
        return saved;
    }

    @Transactional
    public BatchSchedule addScheduleSession(String batchId, String title, Instant scheduledAt, int durationMinutes, String location, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";

        TrainingBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));

        BatchSchedule schedule = BatchSchedule.create(batch.getId(), title, scheduledAt, durationMinutes, location);
        batch.addSchedule(schedule);
        batchRepository.save(batch);

        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, batch.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Added schedule session title='" + title + "' to batch id=" + batch.getId());

        log.info("Added BatchSchedule session id={} to batch id={}", schedule.getId(), batchId);
        return schedule;
    }

    @Transactional(readOnly = true)
    public TrainingBatch getBatchById(String batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for id: " + batchId));
    }

    @Transactional(readOnly = true)
    public TrainingBatch getBatchByCode(String batchCode) {
        return batchRepository.findByBatchCode(batchCode)
                .orElseThrow(() -> new BatchNotFoundException("TrainingBatch not found for code: " + batchCode));
    }

    @Transactional(readOnly = true)
    public Page<TrainingBatch> searchBatches(String programId, BatchStatus status, DeliveryMode deliveryMode, Instant fromDate, Instant toDate, Pageable pageable) {
        return batchRepository.searchBatches(programId, status, deliveryMode, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TrainingBatch> listPublicActiveBatches(Pageable pageable) {
        return batchRepository.findPublicActiveBatches(pageable);
    }

    @Transactional(readOnly = true)
    public TrainingBatch getPublicActiveBatch(String idOrCode) {
        TrainingBatch batch = batchRepository.findById(idOrCode)
                .orElseGet(() -> batchRepository.findByBatchCode(idOrCode)
                        .orElseThrow(() -> new BatchNotFoundException("Active TrainingBatch not found for id or code: " + idOrCode)));

        if (batch.getStatus() != BatchStatus.ACTIVE && batch.getStatus() != BatchStatus.SCHEDULED && batch.getStatus() != BatchStatus.FULL) {
            throw new BatchNotFoundException("Active TrainingBatch not found for id or code: " + idOrCode);
        }
        return batch;
    }
}
