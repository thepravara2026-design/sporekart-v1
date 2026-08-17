package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.BatchSchedule;

import java.time.Instant;

public class ScheduleResponse {

    private String id;
    private String batchId;
    private String title;
    private Instant scheduledAt;
    private int durationMinutes;
    private String location;

    public ScheduleResponse() {}

    public ScheduleResponse(String id, String batchId, String title, Instant scheduledAt, int durationMinutes, String location) {
        this.id = id;
        this.batchId = batchId;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.location = location;
    }

    public static ScheduleResponse fromDomain(BatchSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getBatchId(),
                schedule.getTitle(),
                schedule.getScheduledAt(),
                schedule.getDurationMinutes(),
                schedule.getLocation()
        );
    }

    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getTitle() { return title; }
    public Instant getScheduledAt() { return scheduledAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getLocation() { return location; }
}
