package com.sporekart.modules.review.domain.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String reference) {
        super("Product review not found: " + reference);
    }
}
