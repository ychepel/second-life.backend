package de.ait.secondlife.exception_handling.exceptions;

public class DuplicateUserEmailException extends RuntimeException {

    public DuplicateUserEmailException() {
    }

    public DuplicateUserEmailException(String email) {
        super(String.format("Email %s already exists", email));
    }

    public DuplicateUserEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}