package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.OfferContext;
import de.ait.secondlife.services.interfaces.OfferService;

import java.util.Comparator;
import java.util.List;

public class AuctionFinishedState extends StateStrategy {

    private OfferContext context;

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
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }

    @Override
    public void complete(OfferContext context, Long winnerBidId) {
        this.context = context;
        Offer offer = context.getOffer();
        OfferService offerService = context.getOfferService();
        EmailService emailService = context.getEmailService();

        List<Bid> bids = offer.getBids();

        if (bids == null || bids.isEmpty()) {
            completeWithoutBids(offer, offerService, emailService);
        } else if (bids.size() == 1) {
            completeWithSingleBid(offer, offerService, emailService);
        } else if (!offer.getIsFree() && offer.getMaxBidValue().compareTo(offer.getWinBid()) == 0) {
            completeWithMaxBid(offer, offerService, emailService);
        } else {
            qualify(context);
        }
    }

    private void completeWithMaxBid(Offer offer, OfferService offerService, EmailService emailService) {
        if (offer.getWinnerBid() != null) {
            throw new IllegalStateException(String.format("Offer [ID=%d] already has a winner", offer.getId()));
        }
        Bid maxBid = offer.getBids().stream()
                .max(Comparator.comparing(Bid::getBidValue))
                .get();
        offer.setWinnerBid(maxBid);
        offerService.setStatus(offer, OfferStatus.COMPLETED);

        emailService.createNotification(
                offer.getUser(),
                NotificationType.OFFER_COMPLETION_DUE_TO_BUYOUT_WIN_BID_EMAIL,
                offer.getId()
        );

        emailService.createNotification(
                offer.getWinnerBid().getUser(),
                NotificationType.PARTICIPANT_IS_WINNER_EMAIL,
                offer.getId()
        );

        List<User> loserParticipants = offerService.getNotWinners(offer);
        for (User user : loserParticipants) {
            emailService.createNotification(
                    user,
                    NotificationType.PARTICIPANT_IS_NOT_WINNER_EMAIL,
                    offer.getId());
        }

        context.setStateStrategy(new CompleteState());
    }

    private void completeWithSingleBid(Offer offer, OfferService offerService, EmailService emailService) {
        if (offer.getWinnerBid() != null) {
            throw new IllegalStateException(String.format("Offer [ID=%d] already has a winner", offer.getId()));
        }
        Bid winnerBid = offer.getBids().get(0);
        offer.setWinnerBid(winnerBid );
        offerService.setStatus(offer, OfferStatus.COMPLETED);

        emailService.createNotification(
                offer.getUser(),
                NotificationType.OFFER_COMPLETION_DUE_TO_1_BID_EMAIL_TO_OFFER_OWNER_EMAIL,
                offer.getId()
        );

        emailService.createNotification(
                offer.getWinnerBid().getUser(),
                NotificationType.PARTICIPANT_IS_WINNER_EMAIL,
                offer.getId()
        );

        context.setStateStrategy(new CompleteState());
    }

    private void completeWithoutBids(Offer offer, OfferService offerService, EmailService emailService) {
        offerService.setStatus(offer, OfferStatus.COMPLETED);
        emailService.createNotification(
                offer.getUser(),
                NotificationType.OFFER_COMPLETION_DUE_TO_NO_BIDS_EMAIL,
                offer.getId()
        );
        context.setStateStrategy(new CompleteState());
    }

    @Override
    public void qualify(OfferContext context) {
        Offer offer = context.getOffer();
        List<Bid> bids = offer.getBids();
        if (bids.size() <= 1) {
            throw new IllegalStateException(
                    String.format("Offer [ID=<%d>] has no/one winner and not require status [%s]", offer.getId(), OfferStatus.QUALIFICATION)
            );
        }
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.QUALIFICATION);

        EmailService emailService = context.getEmailService();

        emailService.createNotification(
                offer.getUser(),
                NotificationType.OFFER_QUALIFICATION_EMAIL,
                offer.getId());

        context.setStateStrategy(new QualificationState());
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
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }
}
