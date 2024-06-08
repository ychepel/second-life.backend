package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class UserNotFoundException extends ParameterNotFoundException{

    public UserNotFoundException(Long id) {
        super(String.format("User with %d id not found", id));
    }
}
