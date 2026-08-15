package com.sporekart.modules.returns.domain;

public class ReturnEligibilityException extends RuntimeException {
    private final String reasonCode;

    public ReturnEligibilityException(String reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
