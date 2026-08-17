package com.sporekart.modules.training.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingProgram {

    private final String id;
    private String title;
    private String description;
    private ProgramStatus status;
    private BigDecimal priceAmount;
    private String currency;
    private final Instant createdAt;
    private Instant updatedAt;

    public TrainingProgram(String id, String title, String description, ProgramStatus status, BigDecimal priceAmount, String currency, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = Objects.requireNonNull(title, "Program title must not be null").trim();
        this.description = description;
        this.status = status != null ? status : ProgramStatus.DRAFT;
        this.priceAmount = priceAmount != null ? priceAmount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "INR";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static TrainingProgram create(String title, String description, BigDecimal priceAmount, String currency) {
        return new TrainingProgram(null, title, description, ProgramStatus.DRAFT, priceAmount, currency, Instant.now(), Instant.now());
    }

    public void publish() {
        this.status = ProgramStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = ProgramStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String title, String description, BigDecimal priceAmount, String currency) {
        if (title != null && !title.isBlank()) {
            this.title = title.trim();
        }
        this.description = description;
        if (priceAmount != null) {
            this.priceAmount = priceAmount;
        }
        if (currency != null) {
            this.currency = currency;
        }
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ProgramStatus getStatus() { return status; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingProgram that = (TrainingProgram) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
