package com.sporekart.modules.review.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequestDto(
        @NotBlank(message = "Product ID is required")
        String productId,

        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "Order item ID is required")
        String orderItemId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1 star")
        @Max(value = 5, message = "Rating cannot exceed 5 stars")
        Integer rating,

        @Min(1) @Max(5)
        Integer qualityRating,

        @Min(1) @Max(5)
        Integer valueRating,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        @NotBlank(message = "Comment is required")
        String comment,

        String mediaUrls
) {}
