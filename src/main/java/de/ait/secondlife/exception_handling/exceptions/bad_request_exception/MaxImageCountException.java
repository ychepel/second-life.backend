package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;


public class MaxImageCountException extends BadRequestException{
    public MaxImageCountException(int count) {

        super(String.format("Number of images cannot be greater than <%d>",count));
    }
}
