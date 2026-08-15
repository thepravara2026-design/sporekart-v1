package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderTransitionRequestDto(
        @NotNull(message = "Target status cannot be null")
        OrderStatus targetStatus,

        String reason
) {}
