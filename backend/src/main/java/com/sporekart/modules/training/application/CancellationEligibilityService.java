package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException;
import com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
public class CancellationEligibilityService {

    public static final long TRAINEE_DEADLINE_DAYS = 2L;
    public static final long ADMIN_DEADLINE_DAYS = 7L;

    public void validateTraineeCancellationEligibility(TrainingBatch batch) {
        Objects.requireNonNull(batch, "Training batch must not be null");
        Instant deadline = batch.getStartDate().minus(Duration.ofDays(TRAINEE_DEADLINE_DAYS));
        if (Instant.now().isAfter(deadline)) {
            throw new CancellationWindowExpiredException("Trainee cancellation window expired. Trainees may only cancel up to " + TRAINEE_DEADLINE_DAYS + " days before the scheduled training date (" + batch.getStartDate() + ")");
        }
    }

    public void validateAdminCancellationEligibility(TrainingBatch batch) {
        Objects.requireNonNull(batch, "Training batch must not be null");
        Instant deadline = batch.getStartDate().minus(Duration.ofDays(ADMIN_DEADLINE_DAYS));
        if (Instant.now().isAfter(deadline)) {
            throw new CancellationWindowExpiredException("Admin cancellation window expired. Admins may only cancel up to " + ADMIN_DEADLINE_DAYS + " days before the scheduled training date (" + batch.getStartDate() + ")");
        }
    }

    public void validateTraineeRescheduleEligibility(TrainingBatch batch) {
        Objects.requireNonNull(batch, "Training batch must not be null");
        Instant deadline = batch.getStartDate().minus(Duration.ofDays(TRAINEE_DEADLINE_DAYS));
        if (Instant.now().isAfter(deadline)) {
            throw new RescheduleWindowExpiredException("Trainee reschedule window expired. Trainees may only reschedule up to " + TRAINEE_DEADLINE_DAYS + " days before the scheduled training date (" + batch.getStartDate() + ")");
        }
    }

    public void validateAdminRescheduleEligibility(TrainingBatch batch) {
        Objects.requireNonNull(batch, "Training batch must not be null");
        Instant deadline = batch.getStartDate().minus(Duration.ofDays(ADMIN_DEADLINE_DAYS));
        if (Instant.now().isAfter(deadline)) {
            throw new RescheduleWindowExpiredException("Admin reschedule window expired. Admins may only reschedule up to " + ADMIN_DEADLINE_DAYS + " days before the scheduled training date (" + batch.getStartDate() + ")");
        }
    }
}
