package com.sporekart.modules.training.controller.dto;

public class BatchDemandSummaryResponse {

    private String batchId;
    private long activeDemandCount;

    public BatchDemandSummaryResponse() {
    }

    public BatchDemandSummaryResponse(String batchId, long activeDemandCount) {
        this.batchId = batchId;
        this.activeDemandCount = activeDemandCount;
    }

    public String getBatchId() {
        return batchId;
    }

    public long getActiveDemandCount() {
        return activeDemandCount;
    }
}
