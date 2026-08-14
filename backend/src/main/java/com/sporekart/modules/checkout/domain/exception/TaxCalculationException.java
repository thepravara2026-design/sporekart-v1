package com.sporekart.modules.checkout.domain.exception;

public class TaxCalculationException extends RuntimeException {
    public TaxCalculationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaxCalculationException(String message) {
        super(message);
    }
}
