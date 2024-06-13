package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;


public class BadFileSizeException extends BadRequestException {
    public BadFileSizeException(String fileName, long size, long maxSize) {

        super(String.format(
                "File <%s> size <%d> is larger than allowed <%d>"
                , fileName
                , size
                , maxSize));

    }
}
