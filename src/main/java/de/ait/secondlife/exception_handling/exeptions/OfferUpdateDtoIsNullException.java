package de.ait.secondlife.exception_handling.exeptions;

public class OfferUpdateDtoIsNullException extends RuntimeException{
    public OfferUpdateDtoIsNullException() {

        super("OfferUpdateDto is null");
    }
}
