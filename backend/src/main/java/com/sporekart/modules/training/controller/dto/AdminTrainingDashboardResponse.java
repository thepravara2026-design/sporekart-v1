package com.sporekart.modules.training.controller.dto;

import java.time.Instant;

public class AdminTrainingDashboardResponse {

    private long activeProgramsCount;
    private long upcomingBatchesCount;
    private long totalConfiguredCapacity;
    private long totalOccupiedSeats;
    private long totalRemainingSeats;
    private long activeDemandCount;
    private long totalEnrollmentsCount;
    private long paymentPendingEnrollmentsCount;
    private long paymentFailedEnrollmentsCount;
    private long paymentVerifiedExceptionsCount;
    private long batchesApproachingFullCount;
    private Instant generatedAt;

    public AdminTrainingDashboardResponse() {
        this.generatedAt = Instant.now();
    }

    public AdminTrainingDashboardResponse(long activeProgramsCount, long upcomingBatchesCount,
                                          long totalConfiguredCapacity, long totalOccupiedSeats,
                                          long totalRemainingSeats, long activeDemandCount,
                                          long totalEnrollmentsCount, long paymentPendingEnrollmentsCount,
                                          long paymentFailedEnrollmentsCount, long paymentVerifiedExceptionsCount,
                                          long batchesApproachingFullCount) {
        this.activeProgramsCount = activeProgramsCount;
        this.upcomingBatchesCount = upcomingBatchesCount;
        this.totalConfiguredCapacity = totalConfiguredCapacity;
        this.totalOccupiedSeats = totalOccupiedSeats;
        this.totalRemainingSeats = totalRemainingSeats;
        this.activeDemandCount = activeDemandCount;
        this.totalEnrollmentsCount = totalEnrollmentsCount;
        this.paymentPendingEnrollmentsCount = paymentPendingEnrollmentsCount;
        this.paymentFailedEnrollmentsCount = paymentFailedEnrollmentsCount;
        this.paymentVerifiedExceptionsCount = paymentVerifiedExceptionsCount;
        this.batchesApproachingFullCount = batchesApproachingFullCount;
        this.generatedAt = Instant.now();
    }

    public long getActiveProgramsCount() {
        return activeProgramsCount;
    }

    public void setActiveProgramsCount(long activeProgramsCount) {
        this.activeProgramsCount = activeProgramsCount;
    }

    public long getUpcomingBatchesCount() {
        return upcomingBatchesCount;
    }

    public void setUpcomingBatchesCount(long upcomingBatchesCount) {
        this.upcomingBatchesCount = upcomingBatchesCount;
    }

    public long getTotalConfiguredCapacity() {
        return totalConfiguredCapacity;
    }

    public void setTotalConfiguredCapacity(long totalConfiguredCapacity) {
        this.totalConfiguredCapacity = totalConfiguredCapacity;
    }

    public long getTotalOccupiedSeats() {
        return totalOccupiedSeats;
    }

    public void setTotalOccupiedSeats(long totalOccupiedSeats) {
        this.totalOccupiedSeats = totalOccupiedSeats;
    }

    public long getTotalRemainingSeats() {
        return totalRemainingSeats;
    }

    public void setTotalRemainingSeats(long totalRemainingSeats) {
        this.totalRemainingSeats = totalRemainingSeats;
    }

    public long getActiveDemandCount() {
        return activeDemandCount;
    }

    public void setActiveDemandCount(long activeDemandCount) {
        this.activeDemandCount = activeDemandCount;
    }

    public long getTotalEnrollmentsCount() {
        return totalEnrollmentsCount;
    }

    public void setTotalEnrollmentsCount(long totalEnrollmentsCount) {
        this.totalEnrollmentsCount = totalEnrollmentsCount;
    }

    public long getPaymentPendingEnrollmentsCount() {
        return paymentPendingEnrollmentsCount;
    }

    public void setPaymentPendingEnrollmentsCount(long paymentPendingEnrollmentsCount) {
        this.paymentPendingEnrollmentsCount = paymentPendingEnrollmentsCount;
    }

    public long getPaymentFailedEnrollmentsCount() {
        return paymentFailedEnrollmentsCount;
    }

    public void setPaymentFailedEnrollmentsCount(long paymentFailedEnrollmentsCount) {
        this.paymentFailedEnrollmentsCount = paymentFailedEnrollmentsCount;
    }

    public long getPaymentVerifiedExceptionsCount() {
        return paymentVerifiedExceptionsCount;
    }

    public void setPaymentVerifiedExceptionsCount(long paymentVerifiedExceptionsCount) {
        this.paymentVerifiedExceptionsCount = paymentVerifiedExceptionsCount;
    }

    public long getBatchesApproachingFullCount() {
        return batchesApproachingFullCount;
    }

    public void setBatchesApproachingFullCount(long batchesApproachingFullCount) {
        this.batchesApproachingFullCount = batchesApproachingFullCount;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
