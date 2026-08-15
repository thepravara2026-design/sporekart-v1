package com.sporekart.modules.returns.domain;

public class InvalidReturnStateTransitionException extends RuntimeException {
    public InvalidReturnStateTransitionException(ReturnStatus current, ReturnStatus target) {
        super("Invalid return state transition from " + current + " to " + target);
    }

    public InvalidReturnStateTransitionException(String message) {
        super(message);
    }
}
