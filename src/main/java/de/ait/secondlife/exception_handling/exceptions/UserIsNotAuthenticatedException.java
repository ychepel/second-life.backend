package de.ait.secondlife.exception_handling.exceptions;

import javax.security.auth.login.CredentialException;

public class UserIsNotAuthenticatedException extends CredentialException {

    public UserIsNotAuthenticatedException() {
        super("User is not authenticated");
    }

    public UserIsNotAuthenticatedException(String message) {
        super(message);
    }
}
