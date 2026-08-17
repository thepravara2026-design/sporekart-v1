package com.sporekart.modules.training.application;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.event.TrainingProgramActivatedEvent;
import com.sporekart.modules.training.domain.event.TrainingProgramCreatedEvent;
import com.sporekart.modules.training.domain.event.TrainingProgramDeactivatedEvent;
import com.sporekart.modules.training.domain.event.TrainingProgramUpdatedEvent;
import com.sporekart.modules.training.domain.exception.TrainingNotFoundException;
import com.sporekart.modules.training.domain.exception.TrainingProgramAlreadyExistsException;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TrainingProgramApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TrainingProgramApplicationService.class);

    private final TrainingProgramRepository programRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityAuditService auditService;

    public TrainingProgramApplicationService(TrainingProgramRepository programRepository,
                                            ApplicationEventPublisher eventPublisher,
                                            SecurityAuditService auditService) {
        this.programRepository = programRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    @Transactional
    public TrainingProgram createProgram(String title, String description, String category, int durationHours,
                                         BigDecimal priceAmount, String currency, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";
        String candidateSlug = TrainingProgram.generateSlug(title);

        if (programRepository.existsBySlug(candidateSlug)) {
            log.warn("Training program slug conflict detected for slug '{}'", candidateSlug);
            throw new TrainingProgramAlreadyExistsException("A training program with title '" + title + "' already exists");
        }

        TrainingProgram program = TrainingProgram.create(title, description, category, durationHours, priceAmount, currency, effectiveActor);
        TrainingProgram saved = programRepository.save(program);

        eventPublisher.publishEvent(new TrainingProgramCreatedEvent(saved.getId(), saved.getSlug(), saved.getTitle(), saved.getPriceAmount(), saved.getCurrency()));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Created TrainingProgram title='" + saved.getTitle() + "'");

        log.info("Created TrainingProgram id={}, title='{}', slug='{}'", saved.getId(), saved.getTitle(), saved.getSlug());
        return saved;
    }

    @Transactional
    public TrainingProgram updateProgram(String programId, String title, String description, String category,
                                         Integer durationHours, BigDecimal priceAmount, String currency, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";
        TrainingProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + programId));

        if (title != null && !title.isBlank() && !title.trim().equalsIgnoreCase(program.getTitle())) {
            String newSlug = TrainingProgram.generateSlug(title);
            if (!newSlug.equals(program.getSlug()) && programRepository.existsBySlug(newSlug)) {
                throw new TrainingProgramAlreadyExistsException("A training program with title '" + title + "' already exists");
            }
        }

        program.updateDetails(title, description, category, durationHours, priceAmount, currency, effectiveActor);
        TrainingProgram updated = programRepository.save(program);

        eventPublisher.publishEvent(new TrainingProgramUpdatedEvent(updated.getId(), updated.getSlug(), updated.getTitle(), effectiveActor));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, updated.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Updated TrainingProgram title='" + updated.getTitle() + "'");

        log.info("Updated TrainingProgram id={}, title='{}'", updated.getId(), updated.getTitle());
        return updated;
    }

    @Transactional
    public TrainingProgram activateProgram(String programId, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";
        TrainingProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + programId));

        program.activate();
        TrainingProgram saved = programRepository.save(program);

        eventPublisher.publishEvent(new TrainingProgramActivatedEvent(saved.getId(), saved.getSlug(), saved.getTitle(), effectiveActor));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Activated TrainingProgram id=" + saved.getId());

        log.info("Activated TrainingProgram id={}", saved.getId());
        return saved;
    }

    @Transactional
    public TrainingProgram deactivateProgram(String programId, String actorId) {
        String effectiveActor = actorId != null ? actorId : "SYSTEM";
        TrainingProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + programId));

        program.deactivate();
        TrainingProgram saved = programRepository.save(program);

        eventPublisher.publishEvent(new TrainingProgramDeactivatedEvent(saved.getId(), saved.getSlug(), saved.getTitle(), effectiveActor));
        auditService.logEvent(AuditEventType.SECURITY_SYSTEM_ALERT, effectiveActor, saved.getId(), "127.0.0.1", "TrainingModule", AuditStatus.SUCCESS, "Deactivated TrainingProgram id=" + saved.getId());

        log.info("Deactivated TrainingProgram id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public TrainingProgram getProgramById(String programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for id: " + programId));
    }

    @Transactional(readOnly = true)
    public TrainingProgram getProgramBySlug(String slug) {
        return programRepository.findBySlug(slug)
                .orElseThrow(() -> new TrainingNotFoundException("TrainingProgram not found for slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<TrainingProgram> searchPrograms(String searchKey, ProgramStatus status, String category, Pageable pageable) {
        return programRepository.searchPrograms(searchKey, status, category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TrainingProgram> listPublicActivePrograms(Pageable pageable) {
        return programRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public TrainingProgram getPublicActiveProgram(String idOrSlug) {
        TrainingProgram program = programRepository.findById(idOrSlug)
                .orElseGet(() -> programRepository.findBySlug(idOrSlug)
                        .orElseThrow(() -> new TrainingNotFoundException("Active TrainingProgram not found for id or slug: " + idOrSlug)));

        if (program.getStatus() != ProgramStatus.ACTIVE) {
            throw new TrainingNotFoundException("Active TrainingProgram not found for id or slug: " + idOrSlug);
        }
        return program;
    }
}
