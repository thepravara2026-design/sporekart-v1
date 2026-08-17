package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.controller.dto.CapacityResponse;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.event.BatchBecameFullEvent;
import com.sporekart.modules.training.domain.event.BatchCapacityAvailableEvent;
import com.sporekart.modules.training.domain.event.BatchCapacityChangedEvent;
import com.sporekart.modules.training.domain.exception.CapacityBelowOccupancyException;
import com.sporekart.modules.training.domain.exception.InsufficientCapacityException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CapacityApplicationServiceTest {

    private TrainingBatchRepository batchRepository;
    private ApplicationEventPublisher eventPublisher;
    private SecurityAuditService auditService;
    private CapacityApplicationService service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(TrainingBatchRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        auditService = mock(SecurityAuditService.class);
        service = new CapacityApplicationService(batchRepository, eventPublisher, auditService);
    }

    @Test
    @DisplayName("Should allocate slot atomically and publish BatchBecameFullEvent when full")
    void testAllocateSlotSuccess() {
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 1, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();
        batch.allocateSeat(); // Occupied = 1/1, status = FULL

        when(batchRepository.tryAllocateSeatAtomic("batch-123")).thenReturn(true);
        when(batchRepository.findById("batch-123")).thenReturn(Optional.of(batch));

        CapacityResponse response = service.allocateSlot("batch-123");

        assertNotNull(response);
        assertEquals(1, response.getOccupiedSeats());
        assertTrue(response.isFull());
        verify(eventPublisher, times(1)).publishEvent(any(BatchBecameFullEvent.class));
    }

    @Test
    @DisplayName("Should throw InsufficientCapacityException when atomic allocation returns false")
    void testAllocateSlotFull() {
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 5, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        for (int i = 0; i < 5; i++) batch.allocateSeat(); // FULL

        when(batchRepository.tryAllocateSeatAtomic("batch-123")).thenReturn(false);
        when(batchRepository.findById("batch-123")).thenReturn(Optional.of(batch));

        assertThrows(InsufficientCapacityException.class, () -> service.allocateSlot("batch-123"));
    }

    @Test
    @DisplayName("Should update total capacity and emit BatchCapacityChangedEvent and audit log")
    void testUpdateBatchCapacitySuccess() {
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");

        when(batchRepository.findById("batch-123")).thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CapacityResponse response = service.updateBatchCapacity("batch-123", 25, "ADMIN_USER");

        assertEquals(25, response.getTotalCapacity());
        verify(eventPublisher, times(1)).publishEvent(any(BatchCapacityChangedEvent.class));
        verify(auditService, times(1)).logEvent(any(), eq("ADMIN_USER"), eq("BATCH_CAPACITY_UPDATED"), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should reject capacity reduction below occupied seats with CapacityBelowOccupancyException")
    void testUpdateBatchCapacityBelowOccupancy() {
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        for (int i = 0; i < 6; i++) batch.allocateSeat(); // 6 occupied

        when(batchRepository.findById("batch-123")).thenReturn(Optional.of(batch));

        assertThrows(CapacityBelowOccupancyException.class, () -> service.updateBatchCapacity("batch-123", 4, "ADMIN"));
    }
}
