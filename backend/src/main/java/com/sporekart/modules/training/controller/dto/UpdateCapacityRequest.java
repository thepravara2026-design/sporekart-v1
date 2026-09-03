package com.sporekart.modules.training.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCapacityRequest {

    @NotNull(message = "Capacity must not be null")
    @Min(value = 0, message = "Capacity must be greater than or equal to 0")
    private Integer capacity;

    public UpdateCapacityRequest() {}

    public UpdateCapacityRequest(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
