package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.CancellationEligibilityService;
import com.sporekart.modules.training.application.TrainingRescheduleService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.InsufficientCapacityException;
import com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingRescheduleServiceTest {

    @Mock private TrainingEnrollmentRepository enrollmentRepository;
    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingEnrollmentHistoryRepository historyRepository;
    @Mock private CapacityApplicationService capacityService;
    @Mock private SecurityAuditService auditService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private CancellationEligibilityService eligibilityService;
    private TrainingRescheduleService rescheduleService;

    private static final String TRAINEE_A = "trainee-123";
    private static final String TRAINEE_B = "trainee-999";
    private static final String ADMIN_USER = "admin-master";
    private static final String SOURCE_BATCH_ID = "batch-source";
    private static final String TARGET_BATCH_ID = "batch-target";
    private static final String PROGRAM_ID = "prog-100";
    private static final String ENROLLMENT_ID = "enr-777";

    @BeforeEach
    void setUp() {
        eligibilityService = new CancellationEligibilityService();
        rescheduleService = new TrainingRescheduleService(
                enrollmentRepository, batchRepository, historyRepository, capacityService, eligibilityService, auditService, eventPublisher
        );
    }

    @Test
    @DisplayName("Trainee successfully reschedules to target batch (>2 days before start date, capacity available)")
    void traineeReschedulesSuccess() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(5));
        TrainingBatch sourceBatch = createTestBatch(SOURCE_BATCH_ID, PROGRAM_ID, "BATCH-SRC", futureStart);
        TrainingBatch targetBatch = createTestBatch(TARGET_BATCH_ID, PROGRAM_ID, "BATCH-TGT", futureStart.plus(Duration.ofDays(7)));
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, SOURCE_BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(SOURCE_BATCH_ID)).thenReturn(Optional.of(sourceBatch));
        when(batchRepository.findById(TARGET_BATCH_ID)).thenReturn(Optional.of(targetBatch));
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingEnrollment result = rescheduleService.rescheduleEnrollmentByTrainee(ENROLLMENT_ID, TARGET_BATCH_ID, "Date clash", TRAINEE_A);

        assertThat(result.getBatchId()).isEqualTo(TARGET_BATCH_ID);
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);

        verify(capacityService).allocateSlot(TARGET_BATCH_ID);
        verify(capacityService).releaseSlot(SOURCE_BATCH_ID);
        verify(historyRepository).save(any(TrainingEnrollmentHistory.class));
    }

    @Test
    @DisplayName("Trainee rescheduling fails when target batch is FULL and source enrollment is untouched")
    void traineeRescheduleTargetFullFailsSafely() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(5));
        TrainingBatch sourceBatch = createTestBatch(SOURCE_BATCH_ID, PROGRAM_ID, "BATCH-SRC", futureStart);
        TrainingBatch targetBatch = createTestBatch(TARGET_BATCH_ID, PROGRAM_ID, "BATCH-TGT", futureStart.plus(Duration.ofDays(7)));
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, SOURCE_BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(SOURCE_BATCH_ID)).thenReturn(Optional.of(sourceBatch));
        when(batchRepository.findById(TARGET_BATCH_ID)).thenReturn(Optional.of(targetBatch));

        doThrow(new InsufficientCapacityException("Batch is FULL")).when(capacityService).allocateSlot(TARGET_BATCH_ID);

        assertThatThrownBy(() -> rescheduleService.rescheduleEnrollmentByTrainee(ENROLLMENT_ID, TARGET_BATCH_ID, "Wants next batch", TRAINEE_A))
                .isInstanceOf(InsufficientCapacityException.class);

        verify(capacityService, never()).releaseSlot(SOURCE_BATCH_ID);
        verify(enrollmentRepository, never()).save(any());
        assertThat(enrollment.getBatchId()).isEqualTo(SOURCE_BATCH_ID); // Intact
    }

    @Test
    @DisplayName("Trainee rescheduling throws RescheduleWindowExpiredException when within 2 days of start date")
    void traineeRescheduleExpiredThrowsException() {
        Instant nearStart = Instant.now().plus(Duration.ofHours(20)); // < 2 days
        TrainingBatch sourceBatch = createTestBatch(SOURCE_BATCH_ID, PROGRAM_ID, "BATCH-SRC", nearStart);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, SOURCE_BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(SOURCE_BATCH_ID)).thenReturn(Optional.of(sourceBatch));

        assertThatThrownBy(() -> rescheduleService.rescheduleEnrollmentByTrainee(ENROLLMENT_ID, TARGET_BATCH_ID, "Too late", TRAINEE_A))
                .isInstanceOf(RescheduleWindowExpiredException.class)
                .hasMessageContaining("Trainee reschedule window expired");

        verify(capacityService, never()).allocateSlot(any());
    }

    @Test
    @DisplayName("Trainee B attempting to reschedule Trainee A's enrollment throws UnauthorizedEnrollmentAccessException")
    void traineeIdorRescheduleThrowsUnauthorized() {
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, SOURCE_BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> rescheduleService.rescheduleEnrollmentByTrainee(ENROLLMENT_ID, TARGET_BATCH_ID, "IDOR attack", TRAINEE_B))
                .isInstanceOf(UnauthorizedEnrollmentAccessException.class);

        verify(capacityService, never()).allocateSlot(any());
    }

    @Test
    @DisplayName("Rescheduling throws IllegalArgumentException when program ID does not match target batch")
    void programMismatchThrowsException() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(5));
        TrainingBatch sourceBatch = createTestBatch(SOURCE_BATCH_ID, PROGRAM_ID, "BATCH-SRC", futureStart);
        TrainingBatch targetBatchDiffProgram = createTestBatch(TARGET_BATCH_ID, "different-program-id", "BATCH-TGT", futureStart.plus(Duration.ofDays(7)));
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, SOURCE_BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(SOURCE_BATCH_ID)).thenReturn(Optional.of(sourceBatch));
        when(batchRepository.findById(TARGET_BATCH_ID)).thenReturn(Optional.of(targetBatchDiffProgram));

        assertThatThrownBy(() -> rescheduleService.rescheduleEnrollmentByTrainee(ENROLLMENT_ID, TARGET_BATCH_ID, "Switch program", TRAINEE_A))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match source program");

        verify(capacityService, never()).allocateSlot(any());
    }

    private TrainingBatch createTestBatch(String id, String programId, String code, Instant startDate) {
        return new TrainingBatch(
                id, programId, code, startDate, startDate.plus(Duration.ofDays(5)),
                new Capacity(10, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE,
                "Zoom Link", "https://meeting.com", "UTC", "ADMIN", "ADMIN", List.of(), Instant.now(), Instant.now()
        );
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status) {
        return new TrainingEnrollment(
                id, "ENR-001", batchId, traineeId, status, "PAY-REF-100",
                BigDecimal.valueOf(499), "USD", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
