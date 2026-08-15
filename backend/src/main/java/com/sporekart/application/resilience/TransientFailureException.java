package com.sporekart.application.resilience;

/**
 * Exception representing a transient, retryable failure (network timeout, 502/503/504 HTTP error, lock wait timeout).
 */
public class TransientFailureException extends RuntimeException {

    public TransientFailureException(String message) {
        super(message);
    }

    public TransientFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
