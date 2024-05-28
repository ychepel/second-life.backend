package de.ait.secondlife.exception_handling.exceptions.notFoundException;

import java.util.UUID;

public class OfferNotFoundException extends ParameterNotFoundException {
    public OfferNotFoundException(UUID offerId) {

        super(String.format("Offer with id <%s> not found", offerId));
    }
}
