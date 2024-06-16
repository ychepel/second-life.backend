package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;


public class PathWrongException extends BadRequestException{
    public PathWrongException() {

        super("Path of image file is wrong");
    }
}
