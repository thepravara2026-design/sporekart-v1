package com.sporekart.modules.training;

import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.training.application.notification.TrainingReminderScheduler;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingReminderSchedulerTest {

    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingEnrollmentRepository enrollmentRepository;
    @Mock private NotificationApplicationService notificationService;

    private TrainingReminderScheduler reminderScheduler;

    private static final String BATCH_ID = "batch-upcoming";
    private static final String ENROLLMENT_ID = "enr-rem-100";
    private static final String TRAINEE_ID = "trainee-123";

    @BeforeEach
    void setUp() {
        reminderScheduler = new TrainingReminderScheduler(batchRepository, enrollmentRepository, notificationService);
    }

    @Test
    @DisplayName("processUpcomingTrainingReminders sends reminders for sessions starting within 24 hours")
    void sendsRemindersForSessionsWithin24Hours() {
        Instant nearFutureStart = Instant.now().plus(Duration.ofHours(12)); // Within 24h
        TrainingBatch batch = createTestBatch(BATCH_ID, nearFutureStart, BatchStatus.ACTIVE);
        TrainingEnrollment enrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, EnrollmentStatus.CONFIRMED);

        when(batchRepository.findAll()).thenReturn(List.of(batch));
        when(enrollmentRepository.findByBatchId(BATCH_ID)).thenReturn(List.of(enrollment));

        int count = reminderScheduler.processUpcomingTrainingReminders();

        assertThat(count).isEqualTo(1);
        verify(notificationService, times(2)).sendNotification(
                eq("TRAINING_REMINDER_" + ENROLLMENT_ID + "_24H"), eq("TRAINING_UPCOMING_REMINDER"), eq(TRAINEE_ID), eq(TRAINEE_ID),
                any(NotificationChannel.class), eq("TRAINING_UPCOMING_REMINDER"), eq(NotificationCategory.TRAINING),
                eq(TRAINEE_ID), anyMap(), any(), anyString(), any(), any()
        );
    }

    @Test
    @DisplayName("processUpcomingTrainingReminders skips sessions starting after 24 hours")
    void skipsSessionsFarInFuture() {
        Instant farFutureStart = Instant.now().plus(Duration.ofDays(5)); // 5 days > 24h
        TrainingBatch batch = createTestBatch(BATCH_ID, farFutureStart, BatchStatus.ACTIVE);

        when(batchRepository.findAll()).thenReturn(List.of(batch));

        int count = reminderScheduler.processUpcomingTrainingReminders();

        assertThat(count).isEqualTo(0);
        verify(notificationService, never()).sendNotification(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("processUpcomingTrainingReminders skips CANCELLED batches and CANCELLED enrollments")
    void skipsCancelledBatchesAndEnrollments() {
        Instant nearFutureStart = Instant.now().plus(Duration.ofHours(12));
        TrainingBatch cancelledBatch = createTestBatch("batch-cancelled", nearFutureStart, BatchStatus.CANCELLED);
        TrainingBatch activeBatch = createTestBatch(BATCH_ID, nearFutureStart, BatchStatus.ACTIVE);
        TrainingEnrollment cancelledEnrollment = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, EnrollmentStatus.CANCELLED);

        when(batchRepository.findAll()).thenReturn(List.of(cancelledBatch, activeBatch));
        when(enrollmentRepository.findByBatchId(BATCH_ID)).thenReturn(List.of(cancelledEnrollment));

        int count = reminderScheduler.processUpcomingTrainingReminders();

        assertThat(count).isEqualTo(0);
        verify(notificationService, never()).sendNotification(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private TrainingBatch createTestBatch(String id, Instant startDate, BatchStatus status) {
        return new TrainingBatch(
                id, "PROG-1", "BATCH-001", startDate, startDate.plus(Duration.ofDays(5)),
                new Capacity(10, 5), status, DeliveryMode.ONLINE,
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
