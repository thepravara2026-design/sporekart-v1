package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.BatchApplicationService;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.event.BatchActivatedEvent;
import com.sporekart.modules.training.domain.event.BatchCancelledEvent;
import com.sporekart.modules.training.domain.event.BatchScheduledEvent;
import com.sporekart.modules.training.domain.event.TrainingBatchCreatedEvent;
import com.sporekart.modules.training.domain.exception.BatchAlreadyExistsException;
import com.sporekart.modules.training.domain.exception.BatchReassignmentNotAllowedException;
import com.sporekart.modules.training.domain.exception.TrainingProgramNotEligibleException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BatchApplicationServiceTest {

    private TrainingBatchRepository batchRepository;
    private TrainingProgramRepository programRepository;
    private ApplicationEventPublisher eventPublisher;
    private SecurityAuditService auditService;
    private BatchApplicationService service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(TrainingBatchRepository.class);
        programRepository = mock(TrainingProgramRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        auditService = mock(SecurityAuditService.class);

        service = new BatchApplicationService(batchRepository, programRepository, eventPublisher, auditService);
    }

    @Test
    @DisplayName("Should create batch when program exists and batchCode is unique")
    void testCreateBatchSuccess() {
        TrainingProgram program = TrainingProgram.create("Mushroom Cultivation", "Desc", "GEN", 10, new BigDecimal("1000"), "INR", "ADMIN");
        program.activate(); // ACTIVE program

        when(programRepository.findById("prog-123")).thenReturn(Optional.of(program));
        when(batchRepository.findByBatchCode("TRN-2026-001")).thenReturn(Optional.empty());
        when(batchRepository.save(any(TrainingBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch created = service.createBatch(
                "prog-123",
                "TRN-2026-001",
                start,
                end,
                20,
                DeliveryMode.ONLINE,
                null,
                "https://meet.google.com/test",
                "Asia/Kolkata",
                "ADMIN_USER"
        );

        assertNotNull(created);
        assertEquals("TRN-2026-001", created.getBatchCode());
        verify(eventPublisher, times(1)).publishEvent(any(TrainingBatchCreatedEvent.class));
        verify(eventPublisher, times(1)).publishEvent(any(BatchScheduledEvent.class));
        verify(auditService, times(1)).logEvent(any(), eq("ADMIN_USER"), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should reject batch creation for INACTIVE training program")
    void testCreateBatchInactiveProgram() {
        TrainingProgram program = TrainingProgram.create("Mushroom Cultivation", "Desc", "GEN", 10, new BigDecimal("1000"), "INR", "ADMIN");
        program.activate();
        program.deactivate(); // INACTIVE program

        when(programRepository.findById("prog-123")).thenReturn(Optional.of(program));

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        assertThrows(TrainingProgramNotEligibleException.class, () ->
                service.createBatch("prog-123", "TRN-001", start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "ADMIN")
        );
    }

    @Test
    @DisplayName("Should reject creation if batch code already exists")
    void testCreateBatchDuplicateCode() {
        TrainingProgram program = TrainingProgram.create("Mushroom Cultivation", "Desc", "GEN", 10, new BigDecimal("1000"), "INR", "ADMIN");
        program.activate();

        TrainingBatch existing = TrainingBatch.create("prog-123", "TRN-001", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 10);

        when(programRepository.findById("prog-123")).thenReturn(Optional.of(program));
        when(batchRepository.findByBatchCode("TRN-001")).thenReturn(Optional.of(existing));

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        assertThrows(BatchAlreadyExistsException.class, () ->
                service.createBatch("prog-123", "TRN-001", start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "ADMIN")
        );
    }

    @Test
    @DisplayName("Should prohibit programId reassignment during batch update")
    void testProhibitProgramIdReassignment() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);
        TrainingBatch existing = TrainingBatch.create("prog-123", "TRN-001", start, end, 10);

        when(batchRepository.findById("batch-1")).thenReturn(Optional.of(existing));

        assertThrows(BatchReassignmentNotAllowedException.class, () ->
                service.updateBatch("batch-1", "different-prog-456", start, end, "Asia/Kolkata", DeliveryMode.ONLINE, null, null, "ADMIN")
        );
    }

    @Test
    @DisplayName("Should activate batch and publish BatchActivatedEvent")
    void testActivateBatch() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);
        TrainingBatch existing = TrainingBatch.create("prog-123", "TRN-001", start, end, 10);

        when(batchRepository.findById("batch-1")).thenReturn(Optional.of(existing));
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingBatch activated = service.activateBatch("batch-1", "ADMIN");
        assertEquals(BatchStatus.ACTIVE, activated.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(BatchActivatedEvent.class));
    }
}
