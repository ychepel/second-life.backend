package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class CategoryNotFoundException extends ParameterNotFoundException {

    public CategoryNotFoundException(Long id) {
        super(String.format("Category with id <%s> not found", id));
    }
}
