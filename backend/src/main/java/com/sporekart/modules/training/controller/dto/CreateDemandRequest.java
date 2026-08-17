package com.sporekart.modules.training.controller.dto;

public class CreateDemandRequest {

    private String notes;

    public CreateDemandRequest() {
    }

    public CreateDemandRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
