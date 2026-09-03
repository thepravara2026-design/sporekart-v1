package com.sporekart.modules.seller.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TransitionSellerOrderRequestDto(
        @NotBlank(message = "Status is required")
        String status
) {}
