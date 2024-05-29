package de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions;

public class PageableIsNullException extends ParameterIsNullException {
    public PageableIsNullException() {

        super("Pageable is null");
    }
}
