package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;


public class BadFileFormatException extends BadRequestException{
    public BadFileFormatException() {
        super("Bad format of file");
    }
}
