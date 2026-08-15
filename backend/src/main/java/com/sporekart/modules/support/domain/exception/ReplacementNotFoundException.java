package com.sporekart.modules.support.domain.exception;

public class ReplacementNotFoundException extends RuntimeException {
    public ReplacementNotFoundException(String reference) {
        super("Replacement request not found: " + reference);
    }
}
