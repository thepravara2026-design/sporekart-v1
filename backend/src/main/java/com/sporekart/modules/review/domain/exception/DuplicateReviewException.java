package com.sporekart.modules.review.domain.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String orderItemId) {
        super("A review has already been submitted for order item: " + orderItemId);
    }
}
