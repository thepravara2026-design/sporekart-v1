package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TrainingProgramCreatedEvent implements TrainingEvent {
    private final String eventId;
    private final String programId;
    private final String title;
    private final Instant occurredAt;

    public TrainingProgramCreatedEvent(String programId, String title) {
        this.eventId = UUID.randomUUID().toString();
        this.programId = programId;
        this.title = title;
        this.occurredAt = Instant.now();
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return "TRAINING_PROGRAM_CREATED"; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    public String getProgramId() { return programId; }
    public String getTitle() { return title; }
}
