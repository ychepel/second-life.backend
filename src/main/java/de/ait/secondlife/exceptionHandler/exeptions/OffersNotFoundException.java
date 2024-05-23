package de.ait.secondlife.exceptionHandler.exeptions;

public class OffersNotFoundException extends RuntimeException{
    public OffersNotFoundException() {

        super("Offers not found");
    }
}
