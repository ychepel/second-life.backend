package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class UserNotFoundException extends ParameterNotFoundException{
    public UserNotFoundException(Long id) {
        super(String.format("User with id <%s> not found", id));
    }
}
