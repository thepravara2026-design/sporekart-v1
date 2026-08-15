package com.sporekart.modules.support.domain.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String reference) {
        super("Support ticket not found: " + reference);
    }
}
