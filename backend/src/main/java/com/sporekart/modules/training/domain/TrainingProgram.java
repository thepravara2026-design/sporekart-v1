package com.sporekart.modules.training.domain;

import com.sporekart.modules.training.domain.exception.InvalidTrainingProgramDataException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingProgram {

    private final String id;
    private String slug;
    private String title;
    private String description;
    private String category;
    private int durationHours;
    private ProgramStatus status;
    private BigDecimal priceAmount;
    private String currency;
    private String createdBy;
    private String updatedBy;
    private final Instant createdAt;
    private Instant updatedAt;

    public TrainingProgram(String id, String slug, String title, String description, String category,
                           int durationHours, ProgramStatus status, BigDecimal priceAmount, String currency,
                           String createdBy, String updatedBy, Instant createdAt, Instant updatedAt) {
        validateTitle(title);
        validateDuration(durationHours);
        validatePrice(priceAmount);

        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title.trim();
        this.slug = slug != null && !slug.isBlank() ? slug.trim().toLowerCase() : generateSlug(this.title);
        this.description = description;
        this.category = category != null && !category.isBlank() ? category.trim().toUpperCase() : "GENERAL";
        this.durationHours = durationHours;
        this.status = status != null ? status : ProgramStatus.DRAFT;
        this.priceAmount = priceAmount != null ? priceAmount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "INR";
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static TrainingProgram create(String title, String description, BigDecimal priceAmount, String currency) {
        return create(title, description, "GENERAL", 0, priceAmount, currency, "SYSTEM");
    }

    public static TrainingProgram create(String title, String description, String category, int durationHours,
                                        BigDecimal priceAmount, String currency, String actorId) {
        return new TrainingProgram(null, null, title, description, category, durationHours,
                ProgramStatus.DRAFT, priceAmount, currency, actorId, actorId, Instant.now(), Instant.now());
    }

    public void activate() {
        if (this.status == ProgramStatus.ARCHIVED) {
            throw new InvalidTrainingProgramDataException("Cannot activate an archived training program");
        }
        this.status = ProgramStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (this.status == ProgramStatus.ARCHIVED) {
            throw new InvalidTrainingProgramDataException("Cannot deactivate an archived training program");
        }
        this.status = ProgramStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void publish() {
        activate();
    }

    public void archive() {
        this.status = ProgramStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String title, String description, String category, Integer durationHours,
                              BigDecimal priceAmount, String currency, String actorId) {
        if (title != null && !title.isBlank()) {
            validateTitle(title);
            this.title = title.trim();
            this.slug = generateSlug(this.title);
        }
        if (description != null) {
            this.description = description;
        }
        if (category != null && !category.isBlank()) {
            this.category = category.trim().toUpperCase();
        }
        if (durationHours != null) {
            validateDuration(durationHours);
            this.durationHours = durationHours;
        }
        if (priceAmount != null) {
            validatePrice(priceAmount);
            this.priceAmount = priceAmount;
        }
        if (currency != null && !currency.isBlank()) {
            this.currency = currency.trim();
        }
        if (actorId != null) {
            this.updatedBy = actorId;
        }
        this.updatedAt = Instant.now();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidTrainingProgramDataException("Training program title cannot be empty or blank");
        }
    }

    private static void validateDuration(int durationHours) {
        if (durationHours < 0) {
            throw new InvalidTrainingProgramDataException("Training program duration cannot be negative");
        }
    }

    private static void validatePrice(BigDecimal priceAmount) {
        if (priceAmount != null && priceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTrainingProgramDataException("Training program price cannot be negative");
        }
    }

    public static String generateSlug(String text) {
        if (text == null || text.isBlank()) {
            return "program-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").replaceAll("-+", "-");
        return normalized.isEmpty() ? "program-" + UUID.randomUUID().toString().substring(0, 8) : normalized;
    }

    // Getters
    public String getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getDurationHours() { return durationHours; }
    public ProgramStatus getStatus() { return status; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
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
