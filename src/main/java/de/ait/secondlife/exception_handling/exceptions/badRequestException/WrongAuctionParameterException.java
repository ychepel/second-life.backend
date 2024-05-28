package de.ait.secondlife.exception_handling.exceptions.badRequestException;

public class WrongAuctionParameterException extends BadRequestException {
    public WrongAuctionParameterException(String parameter){
        super(String.format("Parameter '%s' is invalid", parameter));
    }
}
