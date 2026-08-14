package com.sporekart.modules.catalog.domain.exception;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String name) {
        super("A category with name '" + name + "' already exists");
    }
}
