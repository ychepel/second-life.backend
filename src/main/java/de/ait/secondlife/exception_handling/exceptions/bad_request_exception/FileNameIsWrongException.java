package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;


public class FileNameIsWrongException extends BadRequestException{
    public FileNameIsWrongException(String fileName) {

        super(String.format("File name is wrong <%s>",fileName));
    }
}
