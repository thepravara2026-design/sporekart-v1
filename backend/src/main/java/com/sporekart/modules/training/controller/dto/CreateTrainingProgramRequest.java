package com.sporekart.modules.training.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateTrainingProgramRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category;

    @Min(value = 0, message = "Duration hours must be non-negative")
    private int durationHours;

    @NotNull(message = "Price amount is required")
    @Min(value = 0, message = "Price amount must be non-negative")
    private BigDecimal priceAmount;

    private String currency = "INR";

    public CreateTrainingProgramRequest() {}

    public CreateTrainingProgramRequest(String title, String description, String category, int durationHours, BigDecimal priceAmount, String currency) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationHours = durationHours;
        this.priceAmount = priceAmount;
        this.currency = currency != null ? currency : "INR";
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public void setPriceAmount(BigDecimal priceAmount) { this.priceAmount = priceAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
