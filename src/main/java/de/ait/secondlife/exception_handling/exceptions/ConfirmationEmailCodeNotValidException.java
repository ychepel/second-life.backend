package de.ait.secondlife.exception_handling.exceptions;

public class ConfirmationEmailCodeNotValidException extends RuntimeException{
    public ConfirmationEmailCodeNotValidException(String code) {
        super(String.format("Confirmation code '%s' is not valid", code));
    }
}
