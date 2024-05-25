package de.ait.secondlife.exceptionHandler.exeptions;

public class WrongAuctionParameterException extends RuntimeException {
    public WrongAuctionParameterException(String parameter){
        super(String.format("Parameter '%s' is invalid", parameter));
    }
}
