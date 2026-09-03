package com.sporekart.modules.training.controller.dto;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public class UpdateTrainingProgramRequest {

    private String title;
    private String description;
    private String category;

    @Min(value = 0, message = "Duration hours must be non-negative")
    private Integer durationHours;

    @Min(value = 0, message = "Price amount must be non-negative")
    private BigDecimal priceAmount;

    private String currency;

    public UpdateTrainingProgramRequest() {}

    public UpdateTrainingProgramRequest(String title, String description, String category, Integer durationHours, BigDecimal priceAmount, String currency) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationHours = durationHours;
        this.priceAmount = priceAmount;
        this.currency = currency;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public void setPriceAmount(BigDecimal priceAmount) { this.priceAmount = priceAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
