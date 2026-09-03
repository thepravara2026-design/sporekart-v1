package com.sporekart.modules.training.application.notification;

import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class TrainingReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrainingReminderScheduler.class);

    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final NotificationApplicationService notificationService;

    public TrainingReminderScheduler(
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            NotificationApplicationService notificationService) {
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService must not be null");
    }

    @Scheduled(cron = "0 */15 * * * *")
    public int processUpcomingTrainingReminders() {
        log.info("Starting upcoming training session reminder processing execution...");

        Instant now = Instant.now();
        Instant windowEnd = now.plus(Duration.ofHours(24));

        List<TrainingBatch> batches = batchRepository.findAll();
        int remindersDispatched = 0;

        for (TrainingBatch batch : batches) {
            if (batch.getStatus() == BatchStatus.CANCELLED || batch.getStatus() == BatchStatus.COMPLETED) {
                continue;
            }

            Instant startDate = batch.getStartDate();
            if (startDate != null && startDate.isAfter(now) && startDate.isBefore(windowEnd)) {
                List<TrainingEnrollment> enrollments = enrollmentRepository.findByBatchId(batch.getId());

                for (TrainingEnrollment enrollment : enrollments) {
                    if (!enrollment.getStatus().isCapacityConsuming()) {
                        continue;
                    }

                    String idempotencyBase = "TRAINING_REMINDER_" + enrollment.getId() + "_24H";

                    Map<String, Object> vars = new HashMap<>();
                    vars.put("enrollmentId", enrollment.getId());
                    vars.put("batchId", batch.getId());
                    vars.put("batchCode", batch.getBatchCode());
                    vars.put("startDate", startDate.toString());
                    vars.put("traineeId", enrollment.getTraineeId());
                    vars.put("message", "Reminder: Your training session for batch " + batch.getBatchCode() + " starts within 24 hours at " + startDate);

                    // Send EMAIL reminder
                    notificationService.sendNotification(
                            idempotencyBase, "TRAINING_UPCOMING_REMINDER", enrollment.getTraineeId(), enrollment.getTraineeId(),
                            NotificationChannel.EMAIL, "TRAINING_UPCOMING_REMINDER", NotificationCategory.TRAINING,
                            enrollment.getTraineeId(), vars, NotificationPriority.HIGH,
                            idempotencyBase + "-EMAIL", null, null
                    );

                    // Send IN_APP reminder
                    notificationService.sendNotification(
                            idempotencyBase, "TRAINING_UPCOMING_REMINDER", enrollment.getTraineeId(), enrollment.getTraineeId(),
                            NotificationChannel.IN_APP, "TRAINING_UPCOMING_REMINDER", NotificationCategory.TRAINING,
                            enrollment.getTraineeId(), vars, NotificationPriority.HIGH,
                            idempotencyBase + "-INAPP", null, null
                    );

                    remindersDispatched++;
                }
            }
        }

        log.info("Upcoming training reminder processing completed. Total reminders processed: {}", remindersDispatched);
        return remindersDispatched;
    }
}
