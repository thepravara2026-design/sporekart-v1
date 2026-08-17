package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;

import java.math.BigDecimal;
import java.time.Instant;

public class TrainingProgramResponse {

    private String id;
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
    private Instant createdAt;
    private Instant updatedAt;

    public TrainingProgramResponse() {}

    public static TrainingProgramResponse fromDomain(TrainingProgram program) {
        TrainingProgramResponse dto = new TrainingProgramResponse();
        dto.id = program.getId();
        dto.slug = program.getSlug();
        dto.title = program.getTitle();
        dto.description = program.getDescription();
        dto.category = program.getCategory();
        dto.durationHours = program.getDurationHours();
        dto.status = program.getStatus();
        dto.priceAmount = program.getPriceAmount();
        dto.currency = program.getCurrency();
        dto.createdBy = program.getCreatedBy();
        dto.updatedBy = program.getUpdatedBy();
        dto.createdAt = program.getCreatedAt();
        dto.updatedAt = program.getUpdatedAt();
        return dto;
    }

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
