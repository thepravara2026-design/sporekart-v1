package com.sporekart.modules.training.controller.dto;

public class CreateEnrollmentRequest {

    private String idempotencyKey;

    public CreateEnrollmentRequest() {}

    public CreateEnrollmentRequest(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
