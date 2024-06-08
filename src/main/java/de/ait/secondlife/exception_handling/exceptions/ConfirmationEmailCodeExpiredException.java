package de.ait.secondlife.exception_handling.exceptions;

public class ConfirmationEmailCodeExpiredException extends RuntimeException{

    public ConfirmationEmailCodeExpiredException(Long codeId, Long userId){
        super(String.format("Confirmation code with id - <%d>, for the user id - <%d>, is expired ",codeId, userId));
    }
}
