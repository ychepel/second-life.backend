package de.ait.secondlife.exception.exeptions;

public class OffersNotFoundException extends RuntimeException{
    public OffersNotFoundException() {

        super("Offers not found");
    }
}
