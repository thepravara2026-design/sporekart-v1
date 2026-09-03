package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.DeliveryMode;

import java.time.Instant;

public class UpdateBatchRequest {

    private String programId;
    private Instant startDate;
    private Instant endDate;
    private String timezone;
    private DeliveryMode deliveryMode;
    private String venueInfo;
    private String meetingUrl;

    public UpdateBatchRequest() {}

    public UpdateBatchRequest(String programId, Instant startDate, Instant endDate, String timezone, DeliveryMode deliveryMode, String venueInfo, String meetingUrl) {
        this.programId = programId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timezone = timezone;
        this.deliveryMode = deliveryMode;
        this.venueInfo = venueInfo;
        this.meetingUrl = meetingUrl;
    }

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public DeliveryMode getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(DeliveryMode deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getVenueInfo() { return venueInfo; }
    public void setVenueInfo(String venueInfo) { this.venueInfo = venueInfo; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }
}
