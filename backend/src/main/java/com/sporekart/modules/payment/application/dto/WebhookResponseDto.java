package com.sporekart.modules.payment.application.dto;

import com.sporekart.modules.payment.domain.WebhookProcessingStatus;

public record WebhookResponseDto(
        WebhookProcessingStatus status,
        String message
) {}
