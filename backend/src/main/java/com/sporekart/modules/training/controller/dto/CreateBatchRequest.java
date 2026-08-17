package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.DeliveryMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateBatchRequest {

    @NotBlank(message = "programId must not be blank")
    private String programId;

    @NotBlank(message = "batchCode must not be blank")
    private String batchCode;

    @NotNull(message = "startDate must not be null")
    private Instant startDate;

    @NotNull(message = "endDate must not be null")
    private Instant endDate;

    @Min(value = 1, message = "totalCapacity must be at least 1")
    private int totalCapacity;

    private DeliveryMode deliveryMode = DeliveryMode.ONLINE;

    private String venueInfo;

    private String meetingUrl;

    private String timezone = "Asia/Kolkata";

    public CreateBatchRequest() {}

    public CreateBatchRequest(String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity, DeliveryMode deliveryMode, String venueInfo, String meetingUrl, String timezone) {
        this.programId = programId;
        this.batchCode = batchCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCapacity = totalCapacity;
        this.deliveryMode = deliveryMode;
        this.venueInfo = venueInfo;
        this.meetingUrl = meetingUrl;
        this.timezone = timezone;
    }

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public DeliveryMode getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(DeliveryMode deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getVenueInfo() { return venueInfo; }
    public void setVenueInfo(String venueInfo) { this.venueInfo = venueInfo; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
