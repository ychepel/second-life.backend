package de.ait.secondlife.exception_handling.exceptions;

import de.ait.secondlife.domain.entity.Offer;

public class ProhibitedOfferStateChangeException extends IllegalStateException {

    public ProhibitedOfferStateChangeException() {
    }

    public ProhibitedOfferStateChangeException(String message) {
        super(message);
    }

    public ProhibitedOfferStateChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProhibitedOfferStateChangeException(Offer offer) {
        super(getDescription(offer));
    }

    private static String getDescription(Offer offer) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTrace[3];

        return String.format(
                "Prohibited attempt to change Offer [ID=%d] with status [%s] in %s.%s()",
                offer.getId(),
                offer.getStatus() != null ? offer.getStatus().getName() : null,
                caller.getClassName(),
                caller.getMethodName()
        );
    }
}