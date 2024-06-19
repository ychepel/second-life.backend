package de.ait.secondlife.constants;

import de.ait.secondlife.services.emails.*;
import de.ait.secondlife.services.interfaces.EmailTemplateService;
import lombok.Getter;

@Getter
public enum NotificationType {

    REGISTRATION_EMAIL(Registration.class),
    OFFER_VERIFICATION_EMAIL(OfferVerification.class),
    REJECTED_OFFER_EMAIL(OfferRejection.class),
    OFFER_COMPLETED_DUE_TO_NO_BIDS_EMAIL(OfferCompletionDueToNoBids .class),
    OFFER_COMPLETED_DUE_TO_1_BID_EMAIL_TO_OFFER_OWNER_EMAIL(OfferCompletionDueTo1BidEmailToOfferOwner.class),
    PARTICIPANT_IS_WINNER_EMAIL(ParticipantIsWinner.class),
    OFFER_COMPLETED_DUE_TO_BUYOUT_WIN_BID_EMAIL(OfferCompletionDueToBuyoutWinBid.class);

    private final Class<? extends EmailTemplateService> serviceClass;

    NotificationType(Class<? extends EmailTemplateService> serviceClass) {
        this.serviceClass = serviceClass;
    }
}
