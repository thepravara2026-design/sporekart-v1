package com.sporekart.modules.review.application.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewHelpfulnessDto(
        @NotNull(message = "Helpfulness boolean flag is required")
        Boolean isHelpful
) {}
