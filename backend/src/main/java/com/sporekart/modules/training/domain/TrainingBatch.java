package com.sporekart.modules.training.domain;

import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;
import com.sporekart.modules.training.domain.exception.InvalidBatchStateException;
import com.sporekart.modules.training.domain.exception.InvalidScheduleException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrainingBatch {

    private final String id;
    private final String programId;
    private String batchCode;
    private Instant startDate;
    private Instant endDate;
    private Capacity capacity;
    private BatchStatus status;
    private DeliveryMode deliveryMode;
    private String venueInfo;
    private String meetingUrl;
    private String timezone;
    private String createdBy;
    private String updatedBy;
    private final List<BatchSchedule> schedules;
    private final Instant createdAt;
    private Instant updatedAt;

    public TrainingBatch(String id, String programId, String batchCode, Instant startDate, Instant endDate, Capacity capacity, BatchStatus status, List<BatchSchedule> schedules, Instant createdAt, Instant updatedAt) {
        this(id, programId, batchCode, startDate, endDate, capacity, status, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", null, null, schedules, createdAt, updatedAt);
    }

    public TrainingBatch(String id, String programId, String batchCode, Instant startDate, Instant endDate, Capacity capacity, BatchStatus status, DeliveryMode deliveryMode, String venueInfo, String meetingUrl, String timezone, String createdBy, String updatedBy, List<BatchSchedule> schedules, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.programId = Objects.requireNonNull(programId, "programId must not be null");
        this.batchCode = Objects.requireNonNull(batchCode, "batchCode must not be null").trim();
        validateScheduleDates(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = Objects.requireNonNull(capacity, "capacity must not be null");
        this.status = status != null ? status : BatchStatus.PLANNED;
        this.deliveryMode = deliveryMode != null ? deliveryMode : DeliveryMode.ONLINE;
        this.venueInfo = venueInfo;
        this.meetingUrl = meetingUrl;
        this.timezone = (timezone != null && !timezone.isBlank()) ? timezone.trim() : "Asia/Kolkata";
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.schedules = schedules != null ? new ArrayList<>(schedules) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static TrainingBatch create(String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity) {
        return create(programId, batchCode, startDate, endDate, totalCapacity, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", null);
    }

    public static TrainingBatch create(String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity, DeliveryMode deliveryMode, String venueInfo, String meetingUrl, String timezone, String createdBy) {
        return new TrainingBatch(
                null,
                programId,
                batchCode,
                startDate,
                endDate,
                Capacity.of(totalCapacity),
                BatchStatus.PLANNED,
                deliveryMode,
                venueInfo,
                meetingUrl,
                timezone,
                createdBy,
                createdBy,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
    }

    private static void validateScheduleDates(Instant startDate, Instant endDate) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        if (!startDate.isBefore(endDate)) {
            throw new InvalidScheduleException("Scheduled start date (" + startDate + ") must be before end date (" + endDate + ")");
        }
    }

    public void updateSchedule(Instant startDate, Instant endDate, String timezone, DeliveryMode deliveryMode, String venueInfo, String meetingUrl, String updatedBy) {
        if (status == BatchStatus.CANCELLED || status == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Cannot update schedule for batch in status: " + status);
        }
        if (startDate != null && endDate != null) {
            validateScheduleDates(startDate, endDate);
            this.startDate = startDate;
            this.endDate = endDate;
        } else if (startDate != null) {
            validateScheduleDates(startDate, this.endDate);
            this.startDate = startDate;
        } else if (endDate != null) {
            validateScheduleDates(this.startDate, endDate);
            this.endDate = endDate;
        }

        if (timezone != null && !timezone.isBlank()) {
            this.timezone = timezone.trim();
        }
        if (deliveryMode != null) {
            this.deliveryMode = deliveryMode;
        }
        if (venueInfo != null) {
            this.venueInfo = venueInfo;
        }
        if (meetingUrl != null) {
            this.meetingUrl = meetingUrl;
        }
        if (updatedBy != null) {
            this.updatedBy = updatedBy;
        }
        this.updatedAt = Instant.now();
    }

    public void allocateSeat() {
        if (status == BatchStatus.FULL || capacity.isFull()) {
            this.status = BatchStatus.FULL;
            throw new BatchFullException("Cannot allocate seat: Batch " + batchCode + " is FULL");
        }
        if (status == BatchStatus.CANCELLED || status == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Cannot allocate seat: Batch is " + status);
        }
        this.capacity = this.capacity.allocateSeat();
        if (this.capacity.isFull()) {
            this.status = BatchStatus.FULL;
        }
        this.updatedAt = Instant.now();
    }

    public void releaseSeat() {
        this.capacity = this.capacity.releaseSeat();
        if (this.status == BatchStatus.FULL && !this.capacity.isFull()) {
            this.status = BatchStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }

    public void updateTotalCapacity(int newTotalCapacity) {
        this.capacity = this.capacity.withTotalCapacity(newTotalCapacity);
        if (this.capacity.isFull()) {
            this.status = BatchStatus.FULL;
        } else if (this.status == BatchStatus.FULL) {
            this.status = BatchStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.status == BatchStatus.CANCELLED || this.status == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Cannot activate batch in status: " + this.status);
        }
        this.status = this.capacity.isFull() ? BatchStatus.FULL : BatchStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (this.status == BatchStatus.CANCELLED || this.status == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Cannot deactivate batch in status: " + this.status);
        }
        this.status = BatchStatus.PLANNED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == BatchStatus.COMPLETED) {
            throw new InvalidBatchStateException("Cannot cancel completed batch " + batchCode);
        }
        this.status = BatchStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (this.status == BatchStatus.CANCELLED) {
            throw new InvalidBatchStateException("Cannot complete cancelled batch " + batchCode);
        }
        this.status = BatchStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void addSchedule(BatchSchedule schedule) {
        Objects.requireNonNull(schedule, "schedule must not be null");
        this.schedules.add(schedule);
        this.updatedAt = Instant.now();
    }

    public void removeSchedule(String scheduleId) {
        this.schedules.removeIf(s -> s.getId().equals(scheduleId));
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getProgramId() { return programId; }
    public String getBatchCode() { return batchCode; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public Capacity getCapacity() { return capacity; }
    public BatchStatus getStatus() { return status; }
    public DeliveryMode getDeliveryMode() { return deliveryMode; }
    public String getVenueInfo() { return venueInfo; }
    public String getMeetingUrl() { return meetingUrl; }
    public String getTimezone() { return timezone; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public List<BatchSchedule> getSchedules() { return Collections.unmodifiableList(schedules); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingBatch that = (TrainingBatch) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
