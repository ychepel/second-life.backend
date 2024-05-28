package de.ait.secondlife.exception_handling.exceptions.badRequestException.isNullExceptions;

public class PageableIsNullException extends ParameterIsNullException {
    public PageableIsNullException() {

        super("Pageable is null");
    }
}
