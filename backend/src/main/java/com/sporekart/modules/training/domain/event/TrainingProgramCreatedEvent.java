package com.sporekart.modules.training.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TrainingProgramCreatedEvent implements TrainingEvent {

    private final String eventId;
    private final String programId;
    private final String slug;
    private final String title;
    private final BigDecimal priceAmount;
    private final String currency;
    private final Instant occurredAt;

    public TrainingProgramCreatedEvent(String programId, String title) {
        this(programId, null, title, BigDecimal.ZERO, "INR");
    }

    public TrainingProgramCreatedEvent(String programId, String slug, String title, BigDecimal priceAmount, String currency) {
        this.eventId = UUID.randomUUID().toString();
        this.programId = programId;
        this.slug = slug;
        this.title = title;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.occurredAt = Instant.now();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "TRAINING_PROGRAM_CREATED";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getProgramId() { return programId; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
}
