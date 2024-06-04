package de.ait.secondlife.exception_handling.exceptions.not_found_exception;

public class OfferNotFoundException extends ParameterNotFoundException {

    public OfferNotFoundException(Long offerId) {
        super(String.format("Offer with id <%s> not found", offerId));
    }
}
