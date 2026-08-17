package com.sporekart.modules.training.application.notification;

import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.event.*;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class TrainingNotificationEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(TrainingNotificationEventProcessor.class);

    private final NotificationApplicationService notificationService;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingProgramRepository programRepository;

    public TrainingNotificationEventProcessor(
            NotificationApplicationService notificationService,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            @Autowired(required = false) TrainingProgramRepository programRepository) {
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService must not be null");
        this.batchRepository = Objects.requireNonNull(batchRepository, "batchRepository must not be null");
        this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository must not be null");
        this.programRepository = programRepository;
    }

    @EventListener
    @Async
    public void handleEnrollmentConfirmed(TrainingEnrollmentConfirmedEvent event) {
        log.info("Processing notification for TrainingEnrollmentConfirmedEvent: enrollmentId={}, batchId={}, traineeId={}",
                event.getEnrollmentId(), event.getBatchId(), event.getTraineeId());

        TrainingBatch batch = batchRepository.findById(event.getBatchId()).orElse(null);
        String batchCode = batch != null ? batch.getBatchCode() : event.getBatchId();

        Map<String, Object> vars = new HashMap<>();
        vars.put("enrollmentId", event.getEnrollmentId());
        vars.put("batchId", event.getBatchId());
        vars.put("batchCode", batchCode);
        vars.put("traineeId", event.getTraineeId());
        vars.put("paymentReference", event.getPaymentReference() != null ? event.getPaymentReference() : "N/A");
        vars.put("message", "Your training enrollment for batch " + batchCode + " has been successfully confirmed!");

        String eventGuid = UUID.randomUUID().toString();
        String idempotencyBase = "ENR-CONF-" + event.getEnrollmentId();

        // Dispatch EMAIL notification
        notificationService.sendNotification(
                eventGuid, "TRAINING_ENROLLMENT_CONFIRMED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.EMAIL, "TRAINING_ENROLLMENT_CONFIRMED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-EMAIL", null, null
        );

        // Dispatch IN_APP notification
        notificationService.sendNotification(
                eventGuid, "TRAINING_ENROLLMENT_CONFIRMED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.IN_APP, "TRAINING_ENROLLMENT_CONFIRMED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleEnrollmentCancelled(TrainingEnrollmentCancelledEvent event) {
        log.info("Processing notification for TrainingEnrollmentCancelledEvent: enrollmentId={}, traineeId={}",
                event.getEnrollmentId(), event.getTraineeId());

        Map<String, Object> vars = new HashMap<>();
        vars.put("enrollmentId", event.getEnrollmentId());
        vars.put("batchId", event.getBatchId());
        vars.put("traineeId", event.getTraineeId());
        vars.put("cancelledByRole", event.getCancelledByRole());
        vars.put("message", "Your training enrollment " + event.getEnrollmentId() + " has been cancelled.");

        String idempotencyBase = event.getEventId() != null ? event.getEventId() : "ENR-CANCEL-" + event.getEnrollmentId();

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_ENROLLMENT_CANCELLED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.EMAIL, "TRAINING_ENROLLMENT_CANCELLED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-EMAIL", null, null
        );

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_ENROLLMENT_CANCELLED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.IN_APP, "TRAINING_ENROLLMENT_CANCELLED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleEnrollmentRescheduled(TrainingEnrollmentRescheduledEvent event) {
        log.info("Processing notification for TrainingEnrollmentRescheduledEvent: enrollmentId={}, targetBatchId={}",
                event.getEnrollmentId(), event.getTargetBatchId());

        TrainingBatch targetBatch = batchRepository.findById(event.getTargetBatchId()).orElse(null);
        String targetCode = targetBatch != null ? targetBatch.getBatchCode() : event.getTargetBatchId();

        Map<String, Object> vars = new HashMap<>();
        vars.put("enrollmentId", event.getEnrollmentId());
        vars.put("sourceBatchId", event.getSourceBatchId());
        vars.put("targetBatchId", event.getTargetBatchId());
        vars.put("targetBatchCode", targetCode);
        vars.put("traineeId", event.getTraineeId());
        vars.put("message", "Your training enrollment " + event.getEnrollmentId() + " has been rescheduled to batch " + targetCode);

        String idempotencyBase = event.getEventId() != null ? event.getEventId() : "ENR-RESCHED-" + event.getEnrollmentId();

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_ENROLLMENT_RESCHEDULED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.EMAIL, "TRAINING_ENROLLMENT_RESCHEDULED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-EMAIL", null, null
        );

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_ENROLLMENT_RESCHEDULED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.IN_APP, "TRAINING_ENROLLMENT_RESCHEDULED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleBatchCancelled(BatchCancelledEvent event) {
        log.info("Processing notification for BatchCancelledEvent: batchId={}", event.getBatchId());

        List<TrainingEnrollment> enrollments = enrollmentRepository.findByBatchId(event.getBatchId());

        for (TrainingEnrollment enrollment : enrollments) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("batchId", event.getBatchId());
            vars.put("traineeId", enrollment.getTraineeId());
            vars.put("message", "Training batch " + event.getBatchId() + " has been cancelled. Please contact support or reschedule.");

            String idempotencyBase = "BATCH-CANCEL-" + event.getBatchId() + "-" + enrollment.getTraineeId();

            notificationService.sendNotification(
                    event.getEventId(), "TRAINING_BATCH_CANCELLED", enrollment.getTraineeId(), enrollment.getTraineeId(),
                    NotificationChannel.EMAIL, "TRAINING_BATCH_CANCELLED", NotificationCategory.TRAINING,
                    enrollment.getTraineeId(), vars, NotificationPriority.HIGH,
                    idempotencyBase + "-EMAIL", null, null
            );

            notificationService.sendNotification(
                    event.getEventId(), "TRAINING_BATCH_CANCELLED", enrollment.getTraineeId(), enrollment.getTraineeId(),
                    NotificationChannel.IN_APP, "TRAINING_BATCH_CANCELLED", NotificationCategory.TRAINING,
                    enrollment.getTraineeId(), vars, NotificationPriority.HIGH,
                    idempotencyBase + "-INAPP", null, null
            );
        }
    }

    @EventListener
    @Async
    public void handleDemandCreated(TrainingDemandCreatedEvent event) {
        log.info("Processing notification for TrainingDemandCreatedEvent: demandId={}, batchId={}, traineeId={}",
                event.getDemandId(), event.getBatchId(), event.getTraineeId());

        Map<String, Object> vars = new HashMap<>();
        vars.put("demandId", event.getDemandId());
        vars.put("batchId", event.getBatchId());
        vars.put("traineeId", event.getTraineeId());
        vars.put("message", "Your demand request for training batch " + event.getBatchId() + " has been received.");

        String eventGuid = UUID.randomUUID().toString();
        String idempotencyBase = "DEMAND-RECV-" + event.getDemandId();

        notificationService.sendNotification(
                eventGuid, "TRAINING_DEMAND_RECEIVED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.IN_APP, "TRAINING_DEMAND_RECEIVED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.NORMAL,
                idempotencyBase + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleBatchBecameFull(BatchBecameFullEvent event) {
        log.info("Processing notification for BatchBecameFullEvent: batchId={}, batchCode={}",
                event.getBatchId(), event.getBatchCode());

        Map<String, Object> vars = new HashMap<>();
        vars.put("batchId", event.getBatchId());
        vars.put("batchCode", event.getBatchCode());
        vars.put("totalCapacity", event.getTotalCapacity());
        vars.put("message", "Training batch " + event.getBatchCode() + " has reached full capacity (" + event.getTotalCapacity() + " seats).");

        String idempotencyBase = "BATCH-FULL-" + event.getBatchId();

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_BATCH_FULL", "ADMIN", "ADMIN",
                NotificationChannel.IN_APP, "TRAINING_BATCH_FULL", NotificationCategory.TRAINING,
                "admin@sporekart.com", vars, NotificationPriority.NORMAL,
                idempotencyBase + "-INAPP", null, null
        );
    }

    @EventListener
    @Async
    public void handleEnrollmentCompleted(TrainingEnrollmentCompletedEvent event) {
        log.info("Processing notification for TrainingEnrollmentCompletedEvent: enrollmentId={}, batchId={}, traineeId={}",
                event.getEnrollmentId(), event.getBatchId(), event.getTraineeId());

        Map<String, Object> vars = new HashMap<>();
        vars.put("enrollmentId", event.getEnrollmentId());
        vars.put("batchId", event.getBatchId());
        vars.put("traineeId", event.getTraineeId());
        vars.put("certificateId", event.getCertificateId() != null ? event.getCertificateId() : "N/A");
        vars.put("message", "Congratulations! You have successfully completed training batch " + event.getBatchId() + " and earned your certificate.");

        String idempotencyBase = "ENR-COMP-" + event.getEnrollmentId();

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_ENROLLMENT_COMPLETED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.EMAIL, "TRAINING_ENROLLMENT_COMPLETED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-EMAIL", null, null
        );

        notificationService.sendNotification(
                event.getEventId(), "TRAINING_ENROLLMENT_COMPLETED", event.getTraineeId(), event.getTraineeId(),
                NotificationChannel.IN_APP, "TRAINING_ENROLLMENT_COMPLETED", NotificationCategory.TRAINING,
                event.getTraineeId(), vars, NotificationPriority.HIGH,
                idempotencyBase + "-INAPP", null, null
        );
    }
}
