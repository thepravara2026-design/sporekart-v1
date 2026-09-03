package com.sporekart.modules.training.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class TraineeEnrollmentDetailResponse {

    private String id;
    private String enrollmentCode;
    private String batchId;
    private String programId;
    private String programTitle;
    private String programCategory;
    private String batchCode;
    private String deliveryMode;
    private String venueInfo;
    private String meetingUrl;
    private String timezone;
    private List<ScheduleResponse> schedules;
    private String enrollmentStatus;
    private String paymentStatusSummary;
    private BigDecimal priceAmount;
    private String currency;
    private String paymentReference;
    private Instant enrolledAt;
    private Instant confirmedAt;
    private Instant activatedAt;
    private Instant completedAt;
    private Instant createdAt;

    public TraineeEnrollmentDetailResponse() {}

    public TraineeEnrollmentDetailResponse(String id, String enrollmentCode, String batchId, String programId,
                                           String programTitle, String programCategory, String batchCode,
                                           String deliveryMode, String venueInfo, String meetingUrl, String timezone,
                                           List<ScheduleResponse> schedules, String enrollmentStatus,
                                           String paymentStatusSummary, BigDecimal priceAmount, String currency,
                                           String paymentReference, Instant enrolledAt, Instant confirmedAt,
                                           Instant activatedAt, Instant completedAt, Instant createdAt) {
        this.id = id;
        this.enrollmentCode = enrollmentCode;
        this.batchId = batchId;
        this.programId = programId;
        this.programTitle = programTitle;
        this.programCategory = programCategory;
        this.batchCode = batchCode;
        this.deliveryMode = deliveryMode;
        this.venueInfo = venueInfo;
        this.meetingUrl = meetingUrl;
        this.timezone = timezone;
        this.schedules = schedules;
        this.enrollmentStatus = enrollmentStatus;
        this.paymentStatusSummary = paymentStatusSummary;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.paymentReference = paymentReference;
        this.enrolledAt = enrolledAt;
        this.confirmedAt = confirmedAt;
        this.activatedAt = activatedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnrollmentCode() {
        return enrollmentCode;
    }

    public void setEnrollmentCode(String enrollmentCode) {
        this.enrollmentCode = enrollmentCode;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public void setProgramTitle(String programTitle) {
        this.programTitle = programTitle;
    }

    public String getProgramCategory() {
        return programCategory;
    }

    public void setProgramCategory(String programCategory) {
        this.programCategory = programCategory;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getVenueInfo() {
        return venueInfo;
    }

    public void setVenueInfo(String venueInfo) {
        this.venueInfo = venueInfo;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<ScheduleResponse> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleResponse> schedules) {
        this.schedules = schedules;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getPaymentStatusSummary() {
        return paymentStatusSummary;
    }

    public void setPaymentStatusSummary(String paymentStatusSummary) {
        this.paymentStatusSummary = paymentStatusSummary;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Instant enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Instant activatedAt) {
        this.activatedAt = activatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
