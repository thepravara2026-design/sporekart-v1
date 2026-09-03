package com.sporekart.application.outbox.dto;

public record OutboxReplayRequest(
        String reason
) {
}
