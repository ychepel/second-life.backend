package de.ait.secondlife.exceptionHandler.exeptions;

public class OfferCreationDtoIsNullException extends RuntimeException{
    public OfferCreationDtoIsNullException() {

        super("OfferCreationDto is null");
    }
}
