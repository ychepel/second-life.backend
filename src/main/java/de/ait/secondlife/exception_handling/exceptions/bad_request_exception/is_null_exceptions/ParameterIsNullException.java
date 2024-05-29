package de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions;

import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadRequestException;

public class ParameterIsNullException extends BadRequestException {


    public ParameterIsNullException(String message) {
        super(message);
    }
}

