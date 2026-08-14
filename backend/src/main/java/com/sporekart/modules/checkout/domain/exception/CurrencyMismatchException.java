package com.sporekart.modules.checkout.domain.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String expected, String actual) {
        super("Currency mismatch in checkout: expected " + expected + " but found " + actual);
    }
}
