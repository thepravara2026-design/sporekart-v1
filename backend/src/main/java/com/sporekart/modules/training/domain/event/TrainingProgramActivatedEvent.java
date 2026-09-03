package com.sporekart.modules.training.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TrainingProgramActivatedEvent implements TrainingEvent {

    private final String eventId;
    private final String programId;
    private final String slug;
    private final String title;
    private final String actorId;
    private final Instant occurredAt;

    public TrainingProgramActivatedEvent(String programId, String slug, String title, String actorId) {
        this.eventId = UUID.randomUUID().toString();
        this.programId = programId;
        this.slug = slug;
        this.title = title;
        this.actorId = actorId;
        this.occurredAt = Instant.now();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "TRAINING_PROGRAM_ACTIVATED";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getProgramId() { return programId; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getActorId() { return actorId; }
}
