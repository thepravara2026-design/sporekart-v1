package com.sporekart.modules.training;

import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.training.application.notification.TrainingNotificationEventProcessor;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.event.*;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingNotificationEventProcessorTest {

    @Mock private NotificationApplicationService notificationService;
    @Mock private TrainingBatchRepository batchRepository;
    @Mock private TrainingEnrollmentRepository enrollmentRepository;

    private TrainingNotificationEventProcessor eventProcessor;

    private static final String BATCH_ID = "batch-101";
    private static final String ENROLLMENT_ID = "enr-777";
    private static final String TRAINEE_ID = "trainee-123";

    @BeforeEach
    void setUp() {
        eventProcessor = new TrainingNotificationEventProcessor(
                notificationService, batchRepository, enrollmentRepository, null
        );
    }

    @Test
    @DisplayName("handleEnrollmentConfirmed dispatches EMAIL and IN_APP notifications")
    void handleEnrollmentConfirmedDispatchesNotifications() {
        TrainingEnrollmentConfirmedEvent event = new TrainingEnrollmentConfirmedEvent(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, "PAY-REF-100");
        TrainingBatch batch = createTestBatch(BATCH_ID);
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        eventProcessor.handleEnrollmentConfirmed(event);

        verify(notificationService, times(2)).sendNotification(
                anyString(), eq("TRAINING_ENROLLMENT_CONFIRMED"), eq(TRAINEE_ID), eq(TRAINEE_ID),
                any(NotificationChannel.class), eq("TRAINING_ENROLLMENT_CONFIRMED"), eq(NotificationCategory.TRAINING),
                eq(TRAINEE_ID), anyMap(), any(), anyString(), any(), any()
        );
    }

    @Test
    @DisplayName("handleEnrollmentCancelled dispatches EMAIL and IN_APP notifications")
    void handleEnrollmentCancelledDispatchesNotifications() {
        TrainingEnrollmentCancelledEvent event = new TrainingEnrollmentCancelledEvent(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, "TRAINEE");

        eventProcessor.handleEnrollmentCancelled(event);

        verify(notificationService, times(2)).sendNotification(
                eq(event.getEventId()), eq("TRAINING_ENROLLMENT_CANCELLED"), eq(TRAINEE_ID), eq(TRAINEE_ID),
                any(NotificationChannel.class), eq("TRAINING_ENROLLMENT_CANCELLED"), eq(NotificationCategory.TRAINING),
                eq(TRAINEE_ID), anyMap(), any(), anyString(), any(), any()
        );
    }

    @Test
    @DisplayName("handleEnrollmentRescheduled dispatches EMAIL and IN_APP notifications for target batch")
    void handleEnrollmentRescheduledDispatchesNotifications() {
        TrainingEnrollmentRescheduledEvent event = new TrainingEnrollmentRescheduledEvent(ENROLLMENT_ID, BATCH_ID, "batch-target", TRAINEE_ID);
        TrainingBatch targetBatch = createTestBatch("batch-target");
        when(batchRepository.findById("batch-target")).thenReturn(Optional.of(targetBatch));

        eventProcessor.handleEnrollmentRescheduled(event);

        verify(notificationService, times(2)).sendNotification(
                eq(event.getEventId()), eq("TRAINING_ENROLLMENT_RESCHEDULED"), eq(TRAINEE_ID), eq(TRAINEE_ID),
                any(NotificationChannel.class), eq("TRAINING_ENROLLMENT_RESCHEDULED"), eq(NotificationCategory.TRAINING),
                eq(TRAINEE_ID), anyMap(), any(), anyString(), any(), any()
        );
    }

    @Test
    @DisplayName("handleBatchCancelled dispatches notifications to all confirmed trainees in the batch")
    void handleBatchCancelledDispatchesToAllTrainees() {
        BatchCancelledEvent event = new BatchCancelledEvent(BATCH_ID, "Admin cancellation");
        TrainingEnrollment enr1 = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID);
        TrainingEnrollment enr2 = createTestEnrollment("enr-888", BATCH_ID, "trainee-456");

        when(enrollmentRepository.findByBatchId(BATCH_ID)).thenReturn(List.of(enr1, enr2));

        eventProcessor.handleBatchCancelled(event);

        verify(notificationService, times(4)).sendNotification(
                eq(event.getEventId()), eq("TRAINING_BATCH_CANCELLED"), anyString(), anyString(),
                any(NotificationChannel.class), eq("TRAINING_BATCH_CANCELLED"), eq(NotificationCategory.TRAINING),
                anyString(), anyMap(), any(), anyString(), any(), any()
        );
    }

    private TrainingBatch createTestBatch(String id) {
        return new TrainingBatch(
                id, "PROG-1", "BATCH-001", Instant.now().plus(Duration.ofDays(5)), Instant.now().plus(Duration.ofDays(10)),
                new Capacity(10, 5), BatchStatus.ACTIVE, DeliveryMode.ONLINE,
                "Zoom Link", "https://meeting.com", "UTC", "ADMIN", "ADMIN", List.of(), Instant.now(), Instant.now()
        );
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, String traineeId) {
        return new TrainingEnrollment(
                id, "ENR-001", batchId, traineeId, EnrollmentStatus.CONFIRMED, "PAY-REF-100",
                BigDecimal.valueOf(499), "INR", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
