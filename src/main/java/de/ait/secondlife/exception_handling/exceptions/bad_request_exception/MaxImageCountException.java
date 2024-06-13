package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;


public class MaxImageCountException extends BadRequestException {
    public MaxImageCountException(String entityType, int count) {

        super(String.format("Number of images for entity <%s> cannot be greater than <%d>", entityType, count));
    }
}
