package de.ait.secondlife.exception_handling.exeptions;

import jakarta.validation.ConstraintViolation;

import java.util.Set;


public class ValidationException extends RuntimeException {

    public <T> ValidationException(Set<ConstraintViolation<T>> violations) {
        super(violations.stream()
                .map(ConstraintViolation::getMessage)
                .reduce((a, x) -> a + ". " + x)
                .orElse("Validation failed"));

    }
}
