package com.sporekart.modules.payment.domain.exception;

public class PaymentProviderUnavailableException extends RuntimeException {

    public PaymentProviderUnavailableException(String provider, String detail) {
        super("Payment provider '" + provider + "' is currently unavailable: " + detail);
    }
}
