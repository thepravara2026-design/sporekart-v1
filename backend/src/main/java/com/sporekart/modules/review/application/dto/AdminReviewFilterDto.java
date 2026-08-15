package com.sporekart.modules.review.application.dto;

import com.sporekart.modules.review.domain.ReviewStatus;

public record AdminReviewFilterDto(
        ReviewStatus status,
        String productId,
        Integer rating
) {}
