package de.ait.secondlife.exception_handling.exceptions.bad_request_exception;

public class WrongAuctionPriceParameterException extends BadRequestException {
    public WrongAuctionPriceParameterException(){
        super("The winning bid is less than the starting price");
    }
}
