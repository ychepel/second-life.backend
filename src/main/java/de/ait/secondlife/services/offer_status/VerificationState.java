package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.OfferService;

import java.time.LocalDateTime;

public class VerificationState extends StateStrategy {

    @Override
    public void draft(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    void reject(OfferContext context, Long rejectionReasonId) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.REJECTED, rejectionReasonId);
        context.setStateStrategy(new RejectedState());

        EmailService emailService = context.getEmailService();
        emailService.createNotification(
                offer.getUser(),
                NotificationType.REJECTED_OFFER_EMAIL,
                offer.getId()
        );
    }

    @Override
    public void verify(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void startAuction(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentAdmin(context);
        LocalDateTime auctionFinishedAt = LocalDateTime.now().plusDays(offer.getAuctionDurationDays());
        offer.setAuctionFinishedAt(auctionFinishedAt);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.AUCTION_STARTED);
        context.setStateStrategy(new AuctionStartedState());
    }

    @Override
    public void finishAuction(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
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
        offer.setIsActive(false);
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }
}
