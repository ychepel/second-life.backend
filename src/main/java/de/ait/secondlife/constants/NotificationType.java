package de.ait.secondlife.constants;

import de.ait.secondlife.services.emails.*;
import de.ait.secondlife.services.interfaces.EmailTemplateService;
import lombok.Getter;

@Getter
public enum NotificationType {

    REGISTRATION_EMAIL(Registration.class),
    OFFER_VERIFICATION_EMAIL(OfferVerification.class),
    OFFER_QUALIFICATION_EMAIL(OfferQualification.class),
    OFFER_BLOCKED_EMAIL(OfferBlocked.class),
    OFFER_CANCELLATION_EMAIL(OfferCancellation.class),
    OFFER_REJECTION_EMAIL(OfferRejection.class),
    OFFER_COMPLETION_DUE_TO_NO_BIDS_EMAIL(OfferCompletionDueToNoBids .class),
    OFFER_COMPLETION_DUE_TO_1_BID_EMAIL_TO_OFFER_OWNER_EMAIL(OfferCompletionDueTo1BidEmailToOfferOwner.class),
    PARTICIPANT_IS_WINNER_EMAIL(ParticipantIsWinner.class),
    PARTICIPANT_IS_NOT_WINNER_EMAIL(ParticipantIsNotWinner.class),
    OFFER_COMPLETION_DUE_TO_BUYOUT_WIN_BID_EMAIL(OfferCompletionDueToBuyoutWinBid.class);

    private final Class<? extends EmailTemplateService> serviceClass;

    NotificationType(Class<? extends EmailTemplateService> serviceClass) {
        this.serviceClass = serviceClass;
    }
}
