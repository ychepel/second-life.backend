package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class BadEntityTypeException extends ParameterNotFoundException {
    public BadEntityTypeException(String type) {

        super(String.format("Entity type <%s> not found",type));
    }
}
