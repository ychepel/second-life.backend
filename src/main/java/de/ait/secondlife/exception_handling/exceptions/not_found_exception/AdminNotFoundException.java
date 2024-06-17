package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class AdminNotFoundException extends ParameterNotFoundException {

    public AdminNotFoundException(Long id) {
        super(String.format("Admin with id <%s> not found", id));
    }
}
