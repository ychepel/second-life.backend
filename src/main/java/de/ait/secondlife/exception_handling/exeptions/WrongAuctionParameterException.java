package de.ait.secondlife.exception_handling.exeptions;

public class WrongAuctionParameterException extends RuntimeException {
    public WrongAuctionParameterException(String parameter){
        super(String.format("Parameter '%s' is invalid", parameter));
    }
}
