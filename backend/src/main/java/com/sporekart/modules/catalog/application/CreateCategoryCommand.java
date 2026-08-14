package com.sporekart.modules.catalog.application;

public record CreateCategoryCommand(
        String name,
        String description
) {}
