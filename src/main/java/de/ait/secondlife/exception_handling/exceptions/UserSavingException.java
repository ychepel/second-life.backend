package de.ait.secondlife.exception_handling.exceptions;

public class UserSavingException extends RuntimeException {

    public UserSavingException() {
    }

    public UserSavingException(String message) {
        super(message);
    }

    public UserSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}