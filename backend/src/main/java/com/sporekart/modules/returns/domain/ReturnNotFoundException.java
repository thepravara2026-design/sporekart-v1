package com.sporekart.modules.returns.domain;

public class ReturnNotFoundException extends RuntimeException {
    public ReturnNotFoundException(String reference) {
        super("Return record not found for reference: " + reference);
    }
}
