package com.sporekart.modules.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderCommand(
        @NotNull(message = "Shipping address is required")
        @Valid
        AddressDto shippingAddress,

        String idempotencyKey,

        String customerNotes
) {}
