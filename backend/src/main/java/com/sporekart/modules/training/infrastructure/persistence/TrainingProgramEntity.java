package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "training_programs")
public class TrainingProgramEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramStatus status;

    @Column(name = "price_amount", nullable = false)
    private BigDecimal priceAmount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TrainingProgramEntity() {}

    public TrainingProgramEntity(String id, String slug, String title, String description, String category,
                                int durationHours, ProgramStatus status, BigDecimal priceAmount, String currency,
                                String createdBy, String updatedBy, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationHours = durationHours;
        this.status = status;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TrainingProgramEntity fromDomain(TrainingProgram domain) {
        return new TrainingProgramEntity(
                domain.getId(),
                domain.getSlug(),
                domain.getTitle(),
                domain.getDescription(),
                domain.getCategory(),
                domain.getDurationHours(),
                domain.getStatus(),
                domain.getPriceAmount(),
                domain.getCurrency(),
                domain.getCreatedBy(),
                domain.getUpdatedBy(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public TrainingProgram toDomain() {
        return new TrainingProgram(id, slug, title, description, category, durationHours, status, priceAmount, currency, createdBy, updatedBy, createdAt, updatedAt);
    }

    // Getters and setters
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
}
