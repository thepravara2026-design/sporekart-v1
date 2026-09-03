package com.sporekart.modules.training.domain.event;

import java.time.Instant;

public interface TrainingEvent {
    String getEventId();
    String getEventType();
    Instant getOccurredAt();
}
