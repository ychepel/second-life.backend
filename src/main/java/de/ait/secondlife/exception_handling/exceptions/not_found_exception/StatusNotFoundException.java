package de.ait.secondlife.exception_handling.exceptions.not_found_exception;


import de.ait.secondlife.constants.OfferStatus;

public class StatusNotFoundException extends ParameterNotFoundException {

    public StatusNotFoundException(Long id) {
        super(String.format("Status with id <%d> not found", id));
    }

    public StatusNotFoundException(OfferStatus offerStatus) {
        super(String.format("Status with name <%s> not found", offerStatus));
    }
}
