package de.ait.secondlife.exception.exeptions;

import java.util.UUID;

public class OfferNotFoundException extends RuntimeException {
    public OfferNotFoundException(UUID offerId) {

        super(String.format("Offer with id <%s> not found", offerId));
    }
}
