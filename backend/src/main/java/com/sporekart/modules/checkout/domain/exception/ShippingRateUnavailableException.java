package com.sporekart.modules.checkout.domain.exception;

public class ShippingRateUnavailableException extends RuntimeException {
    public ShippingRateUnavailableException(String message) {
        super(message);
    }
}
