package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.CancellationEligibilityService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.application.TrainingCancellationService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException;
import com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentHistoryRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
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
class TrainingCancellationServiceTest {

    @Mock private TrainingEnrollmentRepository enrollmentRepository;
    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingEnrollmentHistoryRepository historyRepository;
    @Mock private TrainingEnrollmentPaymentRepository paymentRepository;
    @Mock private CapacityApplicationService capacityService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private SecurityAuditService auditService;

    private CancellationEligibilityService eligibilityService;
    private EnrollmentLifecycleService lifecycleService;
    private TrainingCancellationService cancellationService;

    private static final String TRAINEE_A = "trainee-123";
    private static final String TRAINEE_B = "trainee-999";
    private static final String ADMIN_USER = "admin-master";
    private static final String BATCH_ID = "batch-101";
    private static final String ENROLLMENT_ID = "enr-777";

    @BeforeEach
    void setUp() {
        eligibilityService = new CancellationEligibilityService();
        lifecycleService = new EnrollmentLifecycleService(
                enrollmentRepository, historyRepository, batchRepository, paymentRepository, null, eventPublisher, auditService
        );
        cancellationService = new TrainingCancellationService(
                enrollmentRepository, batchRepository, lifecycleService, capacityService, eligibilityService, eventPublisher
        );
    }

    @Test
    @DisplayName("Trainee successfully cancels eligible enrollment (>2 days before start date)")
    void traineeCancelsEligibleEnrollmentSuccess() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(5));
        TrainingBatch batch = createTestBatch(BATCH_ID, futureStart);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingEnrollment result = cancellationService.cancelEnrollmentByTrainee(ENROLLMENT_ID, "Personal conflict", TRAINEE_A);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        verify(capacityService).releaseSlot(BATCH_ID);
        verify(historyRepository).save(any(TrainingEnrollmentHistory.class));
    }

    @Test
    @DisplayName("Trainee cancellation throws CancellationWindowExpiredException when within 2 days of start date")
    void traineeCancellationExpiredThrowsException() {
        Instant nearStart = Instant.now().plus(Duration.ofHours(24)); // Only 1 day left
        TrainingBatch batch = createTestBatch(BATCH_ID, nearStart);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> cancellationService.cancelEnrollmentByTrainee(ENROLLMENT_ID, "Last minute cancel", TRAINEE_A))
                .isInstanceOf(CancellationWindowExpiredException.class)
                .hasMessageContaining("Trainee cancellation window expired");

        verify(capacityService, never()).releaseSlot(any());
    }

    @Test
    @DisplayName("Trainee B attempting to cancel Trainee A's enrollment throws UnauthorizedEnrollmentAccessException")
    void traineeIdorCancellationThrowsUnauthorized() {
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> cancellationService.cancelEnrollmentByTrainee(ENROLLMENT_ID, "Malicious attempt", TRAINEE_B))
                .isInstanceOf(UnauthorizedEnrollmentAccessException.class)
                .hasMessageContaining("Access denied: You do not own enrollment");

        verify(capacityService, never()).releaseSlot(any());
    }

    @Test
    @DisplayName("Admin successfully cancels eligible enrollment (>7 days before start date)")
    void adminCancelsEligibleEnrollmentSuccess() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(10));
        TrainingBatch batch = createTestBatch(BATCH_ID, futureStart);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingEnrollment result = cancellationService.cancelEnrollmentByAdmin(ENROLLMENT_ID, "Admin cancellation", ADMIN_USER);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        verify(capacityService).releaseSlot(BATCH_ID);
    }

    @Test
    @DisplayName("Admin cancellation throws CancellationWindowExpiredException when within 7 days of start date")
    void adminCancellationExpiredThrowsException() {
        Instant nearStart = Instant.now().plus(Duration.ofDays(4)); // Only 4 days left (< 7 days)
        TrainingBatch batch = createTestBatch(BATCH_ID, nearStart);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_A, EnrollmentStatus.CONFIRMED);

        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> cancellationService.cancelEnrollmentByAdmin(ENROLLMENT_ID, "Admin late cancel", ADMIN_USER))
                .isInstanceOf(CancellationWindowExpiredException.class)
                .hasMessageContaining("Admin cancellation window expired");

        verify(capacityService, never()).releaseSlot(any());
    }

    @Test
    @DisplayName("Cancellation is idempotent if enrollment is already CANCELLED")
    void cancellationIdempotent() {
        TrainingEnrollment cancelledEnrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_A, EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.findById(ENROLLMENT_ID)).thenReturn(Optional.of(cancelledEnrollment));

        TrainingEnrollment result = cancellationService.cancelEnrollmentByTrainee(ENROLLMENT_ID, "Repeat cancel", TRAINEE_A);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        verify(batchRepository, never()).findById(any());
        verify(capacityService, never()).releaseSlot(any());
    }

    private TrainingBatch createTestBatch(String id, Instant startDate) {
        return new TrainingBatch(
                id, "PROG-1", "BATCH-001", startDate, startDate.plus(Duration.ofDays(5)),
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
