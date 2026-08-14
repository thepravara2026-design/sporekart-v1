package com.sporekart.application.exception;

public class ApiErrorResponse {
    private boolean success = false;
    private ApiErrorDetails error;

    public ApiErrorResponse() {}

    public ApiErrorResponse(ApiErrorDetails error) {
        this.success = false;
        this.error = error;
    }

    public static ApiErrorResponse of(String code, String message, String path) {
        return new ApiErrorResponse(new ApiErrorDetails(code, message, path));
    }

    public static ApiErrorResponse of(String code, String message, String path, String requestId) {
        return new ApiErrorResponse(new ApiErrorDetails(code, message, path, requestId));
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ApiErrorDetails getError() {
        return error;
    }

    public void setError(ApiErrorDetails error) {
        this.error = error;
    }
}
