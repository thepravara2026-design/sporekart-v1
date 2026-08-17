package com.sporekart.modules.training.controller.dto;

import java.time.Instant;

public class TraineeTrainingDashboardResponse {

    private long upcomingEnrollmentsCount;
    private long activeEnrollmentsCount;
    private long completedEnrollmentsCount;
    private long pendingEnrollmentsCount;
    private long activeDemandRequestsCount;
    private String nextUpcomingSessionTitle;
    private String nextUpcomingBatchCode;
    private Instant nextUpcomingStartDate;
    private String nextUpcomingDeliveryMode;
    private String nextUpcomingVenueOrMeeting;
    private Instant generatedAt;

    public TraineeTrainingDashboardResponse() {
        this.generatedAt = Instant.now();
    }

    public TraineeTrainingDashboardResponse(long upcomingEnrollmentsCount, long activeEnrollmentsCount,
                                           long completedEnrollmentsCount, long pendingEnrollmentsCount,
                                           long activeDemandRequestsCount, String nextUpcomingSessionTitle,
                                           String nextUpcomingBatchCode, Instant nextUpcomingStartDate,
                                           String nextUpcomingDeliveryMode, String nextUpcomingVenueOrMeeting) {
        this.upcomingEnrollmentsCount = upcomingEnrollmentsCount;
        this.activeEnrollmentsCount = activeEnrollmentsCount;
        this.completedEnrollmentsCount = completedEnrollmentsCount;
        this.pendingEnrollmentsCount = pendingEnrollmentsCount;
        this.activeDemandRequestsCount = activeDemandRequestsCount;
        this.nextUpcomingSessionTitle = nextUpcomingSessionTitle;
        this.nextUpcomingBatchCode = nextUpcomingBatchCode;
        this.nextUpcomingStartDate = nextUpcomingStartDate;
        this.nextUpcomingDeliveryMode = nextUpcomingDeliveryMode;
        this.nextUpcomingVenueOrMeeting = nextUpcomingVenueOrMeeting;
        this.generatedAt = Instant.now();
    }

    public long getUpcomingEnrollmentsCount() {
        return upcomingEnrollmentsCount;
    }

    public void setUpcomingEnrollmentsCount(long upcomingEnrollmentsCount) {
        this.upcomingEnrollmentsCount = upcomingEnrollmentsCount;
    }

    public long getActiveEnrollmentsCount() {
        return activeEnrollmentsCount;
    }

    public void setActiveEnrollmentsCount(long activeEnrollmentsCount) {
        this.activeEnrollmentsCount = activeEnrollmentsCount;
    }

    public long getCompletedEnrollmentsCount() {
        return completedEnrollmentsCount;
    }

    public void setCompletedEnrollmentsCount(long completedEnrollmentsCount) {
        this.completedEnrollmentsCount = completedEnrollmentsCount;
    }

    public long getPendingEnrollmentsCount() {
        return pendingEnrollmentsCount;
    }

    public void setPendingEnrollmentsCount(long pendingEnrollmentsCount) {
        this.pendingEnrollmentsCount = pendingEnrollmentsCount;
    }

    public long getActiveDemandRequestsCount() {
        return activeDemandRequestsCount;
    }

    public void setActiveDemandRequestsCount(long activeDemandRequestsCount) {
        this.activeDemandRequestsCount = activeDemandRequestsCount;
    }

    public String getNextUpcomingSessionTitle() {
        return nextUpcomingSessionTitle;
    }

    public void setNextUpcomingSessionTitle(String nextUpcomingSessionTitle) {
        this.nextUpcomingSessionTitle = nextUpcomingSessionTitle;
    }

    public String getNextUpcomingBatchCode() {
        return nextUpcomingBatchCode;
    }

    public void setNextUpcomingBatchCode(String nextUpcomingBatchCode) {
        this.nextUpcomingBatchCode = nextUpcomingBatchCode;
    }

    public Instant getNextUpcomingStartDate() {
        return nextUpcomingStartDate;
    }

    public void setNextUpcomingStartDate(Instant nextUpcomingStartDate) {
        this.nextUpcomingStartDate = nextUpcomingStartDate;
    }

    public String getNextUpcomingDeliveryMode() {
        return nextUpcomingDeliveryMode;
    }

    public void setNextUpcomingDeliveryMode(String nextUpcomingDeliveryMode) {
        this.nextUpcomingDeliveryMode = nextUpcomingDeliveryMode;
    }

    public String getNextUpcomingVenueOrMeeting() {
        return nextUpcomingVenueOrMeeting;
    }

    public void setNextUpcomingVenueOrMeeting(String nextUpcomingVenueOrMeeting) {
        this.nextUpcomingVenueOrMeeting = nextUpcomingVenueOrMeeting;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
