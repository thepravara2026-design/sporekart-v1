package com.sporekart.modules.catalog.domain.exception;

import java.util.UUID;

public class CategoryDeletionException extends RuntimeException {
    public CategoryDeletionException(UUID categoryId, long productCount) {
        super("Cannot delete category " + categoryId + " because it still contains " + productCount + " products");
    }
}
