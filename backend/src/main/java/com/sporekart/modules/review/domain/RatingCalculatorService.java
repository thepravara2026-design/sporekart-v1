package com.sporekart.modules.review.domain;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingCalculatorService {

    public void updateRatingSummary(ProductRatingSummary summary, List<ProductReview> approvedReviews) {
        summary.recalculate(approvedReviews);
    }
}
