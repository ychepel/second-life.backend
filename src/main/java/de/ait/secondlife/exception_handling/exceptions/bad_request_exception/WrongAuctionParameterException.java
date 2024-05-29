package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;

public class WrongAuctionParameterException extends BadRequestException {
    public WrongAuctionParameterException(String parameter){
        super(String.format("Parameter '%s' is invalid", parameter));
    }
}
