package de.ait.secondlife.exception_handling.exceptions;

import javax.security.auth.login.CredentialException;

public class UserIsNotAuthorizedException extends CredentialException {

    public UserIsNotAuthorizedException() {
        super("User is not authorized");
    }

    public UserIsNotAuthorizedException(String message) {
        super(message);
    }
}
