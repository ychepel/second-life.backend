package de.ait.secondlife.services.offer_status;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception_handling.exceptions.ProhibitedOfferStateChangeException;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.OfferContext;
import de.ait.secondlife.services.interfaces.OfferService;

import java.util.Comparator;
import java.util.List;

public class AuctionFinishedState extends StateStrategy {

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
        Offer offer = context.getOffer();
        OfferService offerService = context.getOfferService();
        List<Bid> bids = offer.getBids();
        EmailService emailService = context.getEmailService();
        if (bids == null || bids.isEmpty()) {
            offerService.setStatus(offer, OfferStatus.COMPLETED);

            emailService.createNotification(
                    offer.getUser(),
                    NotificationType.OFFER_COMPLETED_DUE_TO_NO_BIDS_EMAIL,
                    offer.getId()
            );


            context.setStateStrategy(new CompleteState());
        } else if (bids.size() == 1) {
            if (offer.getWinnerBid() != null) {
                throw new IllegalStateException(String.format("Offer [ID=%d] already has a winner", offer.getId()));
            }
            offer.setWinnerBid(bids.get(0));
            offerService.setStatus(offer, OfferStatus.COMPLETED);

            emailService.createNotification(
                    offer.getUser(),
                    NotificationType.OFFER_COMPLETED_DUE_TO_1_BID_EMAIL_TO_OFFER_OWNER_EMAIL,
                    offer.getId()
            );

            emailService.createNotification(
                    offer.getWinnerBid().getUser(),
                    NotificationType.PARTICIPANT_IS_WINNER_EMAIL,
                    offer.getId()
            );

            context.setStateStrategy(new CompleteState());
        } else if(!offer.getIsFree() && offer.getMaxBidValue().compareTo(offer.getWinBid()) == 0) {
            if (offer.getWinnerBid() != null) {
                throw new IllegalStateException(String.format("Offer [ID=%d] already has a winner", offer.getId()));
            }
            Bid maxBid = bids.stream()
                    .max(Comparator.comparing(Bid::getBidValue))
                    .get();
            offer.setWinnerBid(maxBid);
            offerService.setStatus(offer, OfferStatus.COMPLETED);

            emailService.createNotification(
                    offer.getUser(),
                    NotificationType.OFFER_COMPLETED_DUE_TO_BUYOUT_WIN_BID_EMAIL,
                    offer.getId()
            );

            emailService.createNotification(
                    offer.getWinnerBid().getUser(),
                    NotificationType.PARTICIPANT_IS_WINNER_EMAIL,
                    offer.getId()
            );

            //TODO - inform other participants, that they didnt win an auction

            context.setStateStrategy(new CompleteState());
        } else {
            qualify(context);
        }
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
        //TODO: mailing - inform offer owner about need to choose a winner
        context.setStateStrategy(new QualificationState());
    }

    @Override
    public void cancel(OfferContext context) {
        Offer offer = getOfferAllowedForCurrentUser(context);
        OfferService offerService = context.getOfferService();
        offerService.setStatus(offer, OfferStatus.CANCELED);
        //TODO: mailing - inform all participants that auction was canceled
        context.setStateStrategy(new CancelState());
    }

    @Override
    public void blockByAdmin(OfferContext context) {
        throw new ProhibitedOfferStateChangeException(context.getOffer());
    }
}
