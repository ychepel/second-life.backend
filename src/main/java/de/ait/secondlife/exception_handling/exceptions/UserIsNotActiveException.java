package de.ait.secondlife.exception_handling.exceptions;

import javax.security.auth.login.CredentialException;

public class UserIsNotActiveException extends CredentialException {

    public UserIsNotActiveException() {
        super("User is not active");
    }

    public UserIsNotActiveException(String message) {
        super(message);
    }
}
