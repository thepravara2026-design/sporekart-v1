package com.sporekart.modules.training.domain.policy;

import com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException;
import com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class CancellationPolicy {

    private final int adminWindowDays;
    private final int traineeWindowDays;

    public CancellationPolicy(int adminWindowDays, int traineeWindowDays) {
        if (adminWindowDays < 0 || traineeWindowDays < 0) {
            throw new IllegalArgumentException("Cancellation window days cannot be negative");
        }
        this.adminWindowDays = adminWindowDays;
        this.traineeWindowDays = traineeWindowDays;
    }

    public static CancellationPolicy defaultConfig() {
        return new CancellationPolicy(7, 2);
    }

    public int getAdminWindowDays() {
        return adminWindowDays;
    }

    public int getTraineeWindowDays() {
        return traineeWindowDays;
    }

    public boolean canAdminCancelOrReschedule(Instant scheduledAt, Instant now) {
        Objects.requireNonNull(scheduledAt, "scheduledAt date must not be null");
        Objects.requireNonNull(now, "now timestamp must not be null");
        Instant deadline = scheduledAt.minus(Duration.ofDays(adminWindowDays));
        return !now.isAfter(deadline);
    }

    public boolean canTraineeCancelOrReschedule(Instant scheduledAt, Instant now) {
        Objects.requireNonNull(scheduledAt, "scheduledAt date must not be null");
        Objects.requireNonNull(now, "now timestamp must not be null");
        Instant deadline = scheduledAt.minus(Duration.ofDays(traineeWindowDays));
        return !now.isAfter(deadline);
    }

    public void validateAdminCancellation(Instant scheduledAt, Instant now) {
        if (!canAdminCancelOrReschedule(scheduledAt, now)) {
            throw new CancellationWindowExpiredException(
                    "Admin cancellation window expired. Operations allowed only until " + adminWindowDays + " days before scheduled date (" + scheduledAt + ")"
            );
        }
    }

    public void validateTraineeCancellation(Instant scheduledAt, Instant now) {
        if (!canTraineeCancelOrReschedule(scheduledAt, now)) {
            throw new CancellationWindowExpiredException(
                    "Trainee cancellation window expired. Operations allowed only until " + traineeWindowDays + " days before scheduled date (" + scheduledAt + ")"
            );
        }
    }

    public void validateAdminReschedule(Instant scheduledAt, Instant now) {
        if (!canAdminCancelOrReschedule(scheduledAt, now)) {
            throw new RescheduleWindowExpiredException(
                    "Admin rescheduling window expired. Operations allowed only until " + adminWindowDays + " days before scheduled date (" + scheduledAt + ")"
            );
        }
    }

    public void validateTraineeReschedule(Instant scheduledAt, Instant now) {
        if (!canTraineeCancelOrReschedule(scheduledAt, now)) {
            throw new RescheduleWindowExpiredException(
                    "Trainee rescheduling window expired. Operations allowed only until " + traineeWindowDays + " days before scheduled date (" + scheduledAt + ")"
            );
        }
    }
}
