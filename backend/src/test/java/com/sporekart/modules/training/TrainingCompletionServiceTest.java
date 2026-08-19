package com.sporekart.modules.training;

import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.application.TrainingAttendanceService;
import com.sporekart.modules.training.application.TrainingCertificateService;
import com.sporekart.modules.training.application.TrainingCompletionService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.event.TrainingEnrollmentCompletedEvent;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingCompletionServiceTest {

    @Mock private TrainingEnrollmentRepository enrollmentRepository;
    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingProgramRepository programRepository;
    @Mock private TrainingAttendanceService attendanceService;
    @Mock private TrainingCertificateService certificateService;
    @Mock private EnrollmentLifecycleService lifecycleService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private TrainingCompletionService completionService;

    private static final String BATCH_ID = "batch-completion";
    private static final String ENROLLMENT_ID = "enr-comp-100";
    private static final String TRAINEE_ID = "trainee-grad";

    @BeforeEach
    void setUp() {
        completionService = new TrainingCompletionService(
                enrollmentRepository, batchRepository, programRepository,
                attendanceService, certificateService, lifecycleService, eventPublisher
        );
    }

    @Test
    @DisplayName("evaluateBatchCompletion graduates trainees with attendance >= threshold")
    void evaluateBatchCompletionGraduatesEligibleTrainees() {
        TrainingBatch batch = createTestBatch(BATCH_ID);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, EnrollmentStatus.CONFIRMED);
        TrainingAttendanceService.AttendanceSummary summary = new TrainingAttendanceService.AttendanceSummary(
                ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, 2, 2, 100.0, List.of()
        );
        TrainingCertificate cert = TrainingCertificate.issue(ENROLLMENT_ID, TRAINEE_ID, TRAINEE_ID, "PROG-1", "Program 1", BATCH_ID, "BATCH-001", Instant.now());
        TrainingEnrollment completedEnrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, EnrollmentStatus.COMPLETED);

        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.findByBatchId(BATCH_ID)).thenReturn(List.of(enrollment));
        when(attendanceService.getTraineeAttendanceSummary(ENROLLMENT_ID, null)).thenReturn(summary);
        when(lifecycleService.transitionStatus(eq(ENROLLMENT_ID), eq(EnrollmentStatus.COMPLETED), anyString(), anyString())).thenReturn(completedEnrollment);
        when(certificateService.issueCertificate(any(), any(), any())).thenReturn(cert);

        TrainingCompletionService.BatchCompletionResult result = completionService.evaluateBatchCompletion(BATCH_ID, 80.0, "ADMIN");

        assertThat(result.newlyCompletedCount()).isEqualTo(1);
        assertThat(result.enrollments().get(0).completed()).isTrue();
        verify(eventPublisher, times(1)).publishEvent(any(TrainingEnrollmentCompletedEvent.class));
    }

    @Test
    @DisplayName("evaluateBatchCompletion skips trainees below attendance threshold")
    void evaluateBatchCompletionSkipsIneligibleTrainees() {
        TrainingBatch batch = createTestBatch(BATCH_ID);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, EnrollmentStatus.CONFIRMED);
        TrainingAttendanceService.AttendanceSummary summary = new TrainingAttendanceService.AttendanceSummary(
                ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, 2, 1, 50.0, List.of()
        );

        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.findByBatchId(BATCH_ID)).thenReturn(List.of(enrollment));
        when(attendanceService.getTraineeAttendanceSummary(ENROLLMENT_ID, null)).thenReturn(summary);

        TrainingCompletionService.BatchCompletionResult result = completionService.evaluateBatchCompletion(BATCH_ID, 80.0, "ADMIN");

        assertThat(result.newlyCompletedCount()).isEqualTo(0);
        assertThat(result.enrollments().get(0).completed()).isFalse();
        verify(lifecycleService, never()).transitionStatus(any(), any(), any(), any());
    }

    private TrainingBatch createTestBatch(String id) {
        return new TrainingBatch(
                id, "PROG-1", "BATCH-001", Instant.now(), Instant.now().plus(Duration.ofDays(5)),
                new Capacity(10, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE,
                "Zoom Link", "https://meeting.com", "UTC", "ADMIN", "ADMIN", List.of(), Instant.now(), Instant.now()
        );
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status) {
        return new TrainingEnrollment(
                id, "ENR-001", batchId, traineeId, status, "PAY-REF-100",
                BigDecimal.valueOf(499), "INR", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
