package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.TrainingProgramApplicationService;
import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.event.TrainingProgramActivatedEvent;
import com.sporekart.modules.training.domain.event.TrainingProgramCreatedEvent;
import com.sporekart.modules.training.domain.event.TrainingProgramDeactivatedEvent;
import com.sporekart.modules.training.domain.exception.TrainingNotFoundException;
import com.sporekart.modules.training.domain.exception.TrainingProgramAlreadyExistsException;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingProgramApplicationServiceTest {

    private TrainingProgramRepository programRepository;
    private ApplicationEventPublisher eventPublisher;
    private SecurityAuditService auditService;
    private TrainingProgramApplicationService service;

    @BeforeEach
    void setUp() {
        programRepository = mock(TrainingProgramRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        auditService = mock(SecurityAuditService.class);
        service = new TrainingProgramApplicationService(programRepository, eventPublisher, auditService);
    }

    @Test
    @DisplayName("Should successfully create program, save to repo, publish event, and log audit")
    void testCreateProgramSuccess() {
        when(programRepository.existsBySlug(any())).thenReturn(false);
        when(programRepository.save(any(TrainingProgram.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingProgram program = service.createProgram("Advanced Mycology", "Deep dive", "SCIENCE", 16, new BigDecimal("2500"), "INR", "ADMIN_USER");

        assertNotNull(program);
        assertEquals("Advanced Mycology", program.getTitle());
        assertEquals("advanced-mycology", program.getSlug());
        assertEquals(ProgramStatus.DRAFT, program.getStatus());

        verify(programRepository).save(any(TrainingProgram.class));
        verify(eventPublisher).publishEvent(any(TrainingProgramCreatedEvent.class));
        verify(auditService).logEvent(any(), eq("ADMIN_USER"), eq(program.getId()), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw TrainingProgramAlreadyExistsException if slug exists")
    void testCreateProgramDuplicateSlug() {
        when(programRepository.existsBySlug("advanced-mycology")).thenReturn(true);

        assertThrows(TrainingProgramAlreadyExistsException.class, () ->
                service.createProgram("Advanced Mycology", "Deep dive", "SCIENCE", 16, new BigDecimal("2500"), "INR", "ADMIN_USER")
        );
        verify(programRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should activate program and publish TrainingProgramActivatedEvent")
    void testActivateProgram() {
        TrainingProgram program = TrainingProgram.create("Mycology 101", "Desc", "GEN", 10, new BigDecimal("500"), "INR", "ADMIN");
        when(programRepository.findById(program.getId())).thenReturn(Optional.of(program));
        when(programRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingProgram activated = service.activateProgram(program.getId(), "ADMIN_USER");

        assertEquals(ProgramStatus.ACTIVE, activated.getStatus());
        verify(eventPublisher).publishEvent(any(TrainingProgramActivatedEvent.class));
    }

    @Test
    @DisplayName("Should deactivate program and publish TrainingProgramDeactivatedEvent")
    void testDeactivateProgram() {
        TrainingProgram program = TrainingProgram.create("Mycology 101", "Desc", "GEN", 10, new BigDecimal("500"), "INR", "ADMIN");
        program.activate();
        when(programRepository.findById(program.getId())).thenReturn(Optional.of(program));
        when(programRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingProgram deactivated = service.deactivateProgram(program.getId(), "ADMIN_USER");

        assertEquals(ProgramStatus.INACTIVE, deactivated.getStatus());
        verify(eventPublisher).publishEvent(any(TrainingProgramDeactivatedEvent.class));
    }

    @Test
    @DisplayName("Should throw TrainingNotFoundException when activating non-existent program")
    void testActivateNonExistentProgram() {
        when(programRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        assertThrows(TrainingNotFoundException.class, () -> service.activateProgram("NON_EXISTENT", "ADMIN"));
    }
}
