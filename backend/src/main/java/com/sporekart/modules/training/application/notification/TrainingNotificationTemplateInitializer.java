package com.sporekart.modules.training.application.notification;

import com.sporekart.modules.notification.application.NotificationTemplateService;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TrainingNotificationTemplateInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TrainingNotificationTemplateInitializer.class);

    private final NotificationTemplateService templateService;

    public TrainingNotificationTemplateInitializer(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("Initializing Training Module notification templates...");

            seedTemplate(
                    "TRAINING_ENROLLMENT_CONFIRMED",
                    "Training Enrollment Confirmed",
                    "Notification sent when a trainee enrollment is confirmed.",
                    "Training Enrollment Confirmed - {batchCode}",
                    "Dear Trainee, your enrollment for batch {batchCode} has been confirmed. Enrollment Reference: {enrollmentId}.",
                    "Your enrollment for training batch {batchCode} has been confirmed."
            );

            seedTemplate(
                    "TRAINING_ENROLLMENT_CANCELLED",
                    "Training Enrollment Cancelled",
                    "Notification sent when a training enrollment is cancelled.",
                    "Training Enrollment Cancelled - {enrollmentId}",
                    "Dear Trainee, your enrollment {enrollmentId} has been cancelled. Reason/Actor: {cancelledByRole}.",
                    "Your training enrollment {enrollmentId} has been cancelled."
            );

            seedTemplate(
                    "TRAINING_ENROLLMENT_RESCHEDULED",
                    "Training Enrollment Rescheduled",
                    "Notification sent when a training enrollment is rescheduled.",
                    "Training Rescheduled - {targetBatchCode}",
                    "Dear Trainee, your enrollment {enrollmentId} has been rescheduled to batch {targetBatchCode}.",
                    "Your training enrollment has been rescheduled to batch {targetBatchCode}."
            );

            seedTemplate(
                    "TRAINING_BATCH_CANCELLED",
                    "Training Batch Cancelled",
                    "Notification sent when an entire training batch is cancelled.",
                    "Batch Cancelled - {batchId}",
                    "Dear Trainee, training batch {batchId} has been cancelled. Please contact support or reschedule.",
                    "Training batch {batchId} has been cancelled."
            );

            seedTemplate(
                    "TRAINING_UPCOMING_REMINDER",
                    "Upcoming Training Reminder",
                    "Reminder sent 24 hours before a training session starts.",
                    "Reminder: Training Session Starting Soon - {batchCode}",
                    "Dear Trainee, your training session for batch {batchCode} starts at {startDate}.",
                    "Reminder: Training batch {batchCode} starts within 24 hours."
            );

            seedTemplate(
                    "TRAINING_DEMAND_RECEIVED",
                    "Training Demand Request Received",
                    "Notification sent when a demand request is submitted.",
                    "Training Demand Request Received",
                    "Dear Trainee, your demand request {demandId} for batch {batchId} has been received.",
                    "Your demand request for training batch {batchId} has been received."
            );

            seedTemplate(
                    "TRAINING_BATCH_FULL",
                    "Training Batch Full Alert",
                    "Operational alert when a batch reaches capacity.",
                    "Alert: Training Batch Full - {batchCode}",
                    "Operational Alert: Batch {batchCode} has reached full capacity of {totalCapacity} seats.",
                    "Batch {batchCode} has reached full capacity."
            );

            log.info("Training Module notification templates successfully initialized.");
        } catch (Exception e) {
            log.warn("Failed to initialize training notification templates: {}", e.getMessage());
        }
    }

    private void seedTemplate(String templateCode, String name, String desc,
                              String emailSubject, String emailBody, String inAppBody) {
        NotificationTemplate template = templateService.createTemplate(templateCode, name, desc, NotificationCategory.TRAINING);
        try {
            templateService.createTemplateVersion(templateCode, NotificationChannel.EMAIL, 1, "en-US", emailSubject, emailBody);
        } catch (Exception ignored) {}
        try {
            templateService.createTemplateVersion(templateCode, NotificationChannel.IN_APP, 1, "en-US", "Notification", inAppBody);
        } catch (Exception ignored) {}
    }
}
