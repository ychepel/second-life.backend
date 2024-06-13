package de.ait.secondlife.exception_handling.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class DuplicateCategoryException extends DataIntegrityViolationException {
    public DuplicateCategoryException(String name) {
        super(String.format("Category with the name %s already exists",name));
    }
}
