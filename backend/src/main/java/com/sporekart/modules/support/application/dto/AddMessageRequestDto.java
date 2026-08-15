package com.sporekart.modules.support.application.dto;

import com.sporekart.modules.support.domain.MessageVisibility;
import jakarta.validation.constraints.NotBlank;

public record AddMessageRequestDto(
        @NotBlank(message = "Message content is required")
        String content,

        MessageVisibility visibility,
        String attachmentUrls
) {}
