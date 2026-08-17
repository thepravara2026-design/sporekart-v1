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

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramStatus status;

    @Column(name = "price_amount", nullable = false)
    private BigDecimal priceAmount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TrainingProgramEntity() {}

    public TrainingProgramEntity(String id, String title, String description, ProgramStatus status, BigDecimal priceAmount, String currency, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TrainingProgramEntity fromDomain(TrainingProgram domain) {
        return new TrainingProgramEntity(
                domain.getId(),
                domain.getTitle(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getPriceAmount(),
                domain.getCurrency(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public TrainingProgram toDomain() {
        return new TrainingProgram(id, title, description, status, priceAmount, currency, createdAt, updatedAt);
    }

    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ProgramStatus getStatus() { return status; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
