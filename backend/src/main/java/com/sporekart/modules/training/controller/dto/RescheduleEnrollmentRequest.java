package com.sporekart.modules.training.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class RescheduleEnrollmentRequest {

    @NotBlank(message = "targetBatchId must not be blank")
    private String targetBatchId;

    private String reason;

    public RescheduleEnrollmentRequest() {}

    public RescheduleEnrollmentRequest(String targetBatchId, String reason) {
        this.targetBatchId = targetBatchId;
        this.reason = reason;
    }

    public String getTargetBatchId() {
        return targetBatchId;
    }

    public void setTargetBatchId(String targetBatchId) {
        this.targetBatchId = targetBatchId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
