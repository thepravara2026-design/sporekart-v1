package com.sporekart.modules.training.controller.dto;

public class CancelEnrollmentRequest {

    private String reason;

    public CancelEnrollmentRequest() {}

    public CancelEnrollmentRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
