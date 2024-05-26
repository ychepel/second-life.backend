package de.ait.secondlife.exception_handling.exeptions;

public class OfferCreationDtoIsNullException extends RuntimeException{
    public OfferCreationDtoIsNullException() {

        super("OfferCreationDto is null");
    }
}
