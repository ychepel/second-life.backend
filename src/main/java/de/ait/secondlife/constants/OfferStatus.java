package de.ait.secondlife.constants;

import de.ait.secondlife.exception_handling.exceptions.not_found_exception.StatusNotFoundException;

public enum OfferStatus {
    DRAFT,
    REJECTED,
    VERIFICATION,
    BLOCKED_BY_ADMIN,
    AUCTION_STARTED,
    AUCTION_FINISHED,
    QUALIFICATION,
    COMPLETED,
    CANCELED;

   public static OfferStatus get(String status) {
       try {
           return OfferStatus.valueOf(status.toUpperCase());
       } catch (Exception e) {
          throw new StatusNotFoundException(status);
       }
   }
}

