package de.ait.secondlife.exception_handling.exceptions.badRequestException.isNullExceptions;

import de.ait.secondlife.exception_handling.exceptions.badRequestException.BadRequestException;

public class ParameterIsNullException extends BadRequestException {


    public ParameterIsNullException(String message) {
        super(message);
    }
}

