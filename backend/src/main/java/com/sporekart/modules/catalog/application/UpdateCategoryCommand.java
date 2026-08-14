package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.category.CategoryStatus;

public record UpdateCategoryCommand(
        String name,
        String description,
        CategoryStatus status
) {}
