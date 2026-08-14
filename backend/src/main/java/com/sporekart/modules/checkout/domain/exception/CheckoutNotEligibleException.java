package com.sporekart.modules.checkout.domain.exception;

public class CheckoutNotEligibleException extends RuntimeException {
    public CheckoutNotEligibleException(String message) {
        super(message);
    }
}
