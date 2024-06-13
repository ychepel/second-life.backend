package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;

import org.springframework.dao.DataIntegrityViolationException;

public class BidCreationException extends DataIntegrityViolationException {

    public BidCreationException(String message) {
        super(message);
    }

    public BidCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
