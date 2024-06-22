package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.OfferContext;
import de.ait.secondlife.services.interfaces.OfferService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AuctionStartedState extends StateStrategy {

    @Override
    public void draft(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    void reject(OfferContext context, Long rejectionReasonId) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void verify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void startAuction(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void finishAuction(OfferContext context) {
        Offer offer = context.getOffer();
        log.info(String.format("Finishing auction for Offer [ID=%d]", offer.getId()));
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.AUCTION_FINISHED);
        context.setStateStrategy(new AuctionFinishedState());
        context.complete(null);
    }

    @Override
    public void complete(OfferContext context, Long winnerBidId) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void qualify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void cancel(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentUser(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.CANCELED);
        EmailService emailService = context.getEmailService();

        List<User> participants = offerService.getParticipants(offer);
        for (User user : participants) {
            emailService.createNotification(
                    user,
                    NotificationType.OFFER_CANCELLATION_EMAIL,
                    offer.getId());
        }
        context.setStateStrategy(new CancelState());
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.BLOCKED_BY_ADMIN);

        EmailService emailService = context.getEmailService();
        emailService.createNotification(
                offer.getUser(),
                NotificationType.OFFER_BLOCKED_EMAIL,
                offer.getId());

        context.setStateStrategy(new BlockByAdminState());
    }
}
