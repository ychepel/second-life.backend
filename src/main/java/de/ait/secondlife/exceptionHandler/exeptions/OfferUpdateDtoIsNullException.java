package de.ait.secondlife.exceptionHandler.exeptions;

public class OfferUpdateDtoIsNullException extends RuntimeException{
    public OfferUpdateDtoIsNullException() {

        super("OfferUpdateDto is null");
    }
}
