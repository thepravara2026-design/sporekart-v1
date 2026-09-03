package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class BatchResponse {

    private String id;
    private String programId;
    private String batchCode;
    private Instant startDate;
    private Instant endDate;
    private int totalCapacity;
    private int occupiedSeats;
    private BatchStatus status;
    private DeliveryMode deliveryMode;
    private String venueInfo;
    private String meetingUrl;
    private String timezone;
    private String createdBy;
    private String updatedBy;
    private List<ScheduleResponse> schedules;
    private Instant createdAt;
    private Instant updatedAt;

    public BatchResponse() {}

    public BatchResponse(String id, String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity, int occupiedSeats, BatchStatus status, DeliveryMode deliveryMode, String venueInfo, String meetingUrl, String timezone, String createdBy, String updatedBy, List<ScheduleResponse> schedules, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.programId = programId;
        this.batchCode = batchCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCapacity = totalCapacity;
        this.occupiedSeats = occupiedSeats;
        this.status = status;
        this.deliveryMode = deliveryMode;
        this.venueInfo = venueInfo;
        this.meetingUrl = meetingUrl;
        this.timezone = timezone;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.schedules = schedules;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BatchResponse fromDomain(TrainingBatch batch) {
        List<ScheduleResponse> scheduleResponses = batch.getSchedules().stream()
                .map(ScheduleResponse::fromDomain)
                .collect(Collectors.toList());

        return new BatchResponse(
                batch.getId(),
                batch.getProgramId(),
                batch.getBatchCode(),
                batch.getStartDate(),
                batch.getEndDate(),
                batch.getCapacity().getTotalCapacity(),
                batch.getCapacity().getOccupiedSeats(),
                batch.getStatus(),
                batch.getDeliveryMode(),
                batch.getVenueInfo(),
                batch.getMeetingUrl(),
                batch.getTimezone(),
                batch.getCreatedBy(),
                batch.getUpdatedBy(),
                scheduleResponses,
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }

    public String getId() { return id; }
    public String getProgramId() { return programId; }
    public String getBatchCode() { return batchCode; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getOccupiedSeats() { return occupiedSeats; }
    public BatchStatus getStatus() { return status; }
    public DeliveryMode getDeliveryMode() { return deliveryMode; }
    public String getVenueInfo() { return venueInfo; }
    public String getMeetingUrl() { return meetingUrl; }
    public String getTimezone() { return timezone; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public List<ScheduleResponse> getSchedules() { return schedules; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
