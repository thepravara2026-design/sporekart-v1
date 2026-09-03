package com.sporekart.modules.training.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class AddScheduleRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    @NotNull(message = "scheduledAt must not be null")
    private Instant scheduledAt;

    @Min(value = 1, message = "durationMinutes must be greater than 0")
    private int durationMinutes;

    private String location;

    public AddScheduleRequest() {}

    public AddScheduleRequest(String title, Instant scheduledAt, int durationMinutes, String location) {
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.location = location;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
