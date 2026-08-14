package com.sporekart.common.response;

import java.time.Instant;

public class ApiErrorDetails {
    private String code;
    private String message;
    private String timestamp;
    private String path;

    public ApiErrorDetails() {
        this.timestamp = Instant.now().toString();
    }

    public ApiErrorDetails(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now().toString();
        this.path = path;
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
}
