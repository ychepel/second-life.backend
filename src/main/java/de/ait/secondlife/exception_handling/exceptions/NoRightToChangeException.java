package de.ait.secondlife.exception_handling.exceptions;

public class NoRightToChangeException extends RuntimeException {
    public NoRightToChangeException(Long id) {

        super(String.format("User <%d> can't change this offer", id));
    }
}
