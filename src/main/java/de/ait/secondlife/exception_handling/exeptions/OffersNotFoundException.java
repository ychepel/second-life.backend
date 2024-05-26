package de.ait.secondlife.exception_handling.exeptions;

public class OffersNotFoundException extends RuntimeException{
    public OffersNotFoundException() {

        super("Offers not found");
    }
}
