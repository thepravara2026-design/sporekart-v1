package com.sporekart.modules.catalog.domain.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(UUID id) {
        super("Category not found with ID: " + id);
    }

    public CategoryNotFoundException(String identifier) {
        super("Category not found: " + identifier);
    }
}
