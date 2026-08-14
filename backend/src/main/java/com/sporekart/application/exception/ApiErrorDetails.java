package com.sporekart.application.exception;

import org.slf4j.MDC;
import java.time.Instant;

public class ApiErrorDetails {
    private String code;
    private String message;
    private String timestamp;
    private String path;
    private String requestId;

    public ApiErrorDetails() {
        this.timestamp = Instant.now().toString();
        this.requestId = MDC.get("requestId");
    }

    public ApiErrorDetails(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now().toString();
        this.path = path;
        this.requestId = MDC.get("requestId");
    }

    public ApiErrorDetails(String code, String message, String path, String requestId) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now().toString();
        this.path = path;
        this.requestId = requestId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
