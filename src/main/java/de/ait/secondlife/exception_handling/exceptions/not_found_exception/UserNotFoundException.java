package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserNotFoundException extends UsernameNotFoundException {

    public UserNotFoundException(Long id) {
        super(String.format("User with id <%s> not found", id));
    }

    public UserNotFoundException(String email) {
        super(String.format("User with email <%s> not found", email));
    }
}
