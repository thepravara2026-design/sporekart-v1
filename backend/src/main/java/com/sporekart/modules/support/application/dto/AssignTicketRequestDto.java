package com.sporekart.modules.support.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTicketRequestDto(
        @NotBlank(message = "Agent ID is required")
        String agentId
) {}
