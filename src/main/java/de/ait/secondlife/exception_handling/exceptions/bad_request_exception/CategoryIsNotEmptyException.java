package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;

public class CategoryIsNotEmptyException extends BadRequestException {
    public CategoryIsNotEmptyException(Long id){
        super(String.format("Category id <%d> isn't empty", id));
    }
}
